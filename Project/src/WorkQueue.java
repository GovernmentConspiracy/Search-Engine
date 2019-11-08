import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.LinkedList;

/**
 * A simple work queue implementation based on the IBM Developer article by
 * Brian Goetz. It is up to the user of this class to keep track of whether
 * there is any pending work remaining.
 *
 * @see <a href="https://www.ibm.com/developerworks/library/j-jtp0730/index.html">
 * Java Theory and Practice: Thread Pools and Work Queues</a>
 */
public class WorkQueue {

	private volatile boolean waitingShutdown;
	private int pending;

	private static final Logger log = LogManager.getLogger();

	/**
	 * Pool of worker threads that will wait in the background until work is
	 * available.
	 */
	private final PoolWorker[] workers;

	/**
	 * Queue of pending work requests.
	 */
	private final LinkedList<Runnable> queue;

	/**
	 * Used to signal the queue should be shutdown.
	 */
	private volatile boolean shutdown;

	/**
	 * The default number of threads to use when not specified.
	 */
	public static final int DEFAULT = 5;

	/**
	 * Starts a work queue with the default number of threads.
	 *
	 * @see #WorkQueue(int)
	 */
	public WorkQueue() {
		this(DEFAULT);
	}

	/**
	 * Starts a work queue with the specified number of threads.
	 *
	 * @param threads number of worker threads; should be greater than 1
	 */
	public WorkQueue(int threads) {
		this.queue = new LinkedList<Runnable>();
		this.workers = new PoolWorker[threads];

		this.shutdown = false;
		this.waitingShutdown = false;
		pending = 0;

		// start the threads so they are waiting in the background
		for (int i = 0; i < threads; i++) {
			workers[i] = new PoolWorker();
			workers[i].start();
			log.trace("Worker {} started.", i);
		}
	}


	/**
	 * Adds a work request to the queue. A thread will process this request when
	 * available.
	 *
	 * @param r work request (in the form of a {@link Runnable} object)
	 */
	public void execute(Runnable r) {
		if (!waitingShutdown) {
			synchronized (queue) {
				queue.addLast(r);
				queue.notifyAll();
			}
		}
	}

	/**
	 * Waits for all pending work to be finished.
	 */
	public void finish() throws InterruptedException {
		waitingShutdown = true;

		synchronized (queue) {
			queue.notifyAll();
		}

		while (!queue.isEmpty() || pending > 0) {
			synchronized (this) {
				log.trace("finish() waiting at pending = {}.", pending);
				this.wait(); //TODO FIx
				log.debug("finish() woke up with pending = {}.", pending);
			}
		}
	}

	/**
	 * Asks the queue to shutdown. Any unprocessed work will not be finished,
	 * but threads in-progress will not be interrupted.
	 */
	public void shutdown() {
		// safe to do unsynchronized due to volatile keyword
		shutdown = true;

		synchronized (queue) {
			queue.notifyAll();
		}
	}

	/**
	 * Returns the number of worker threads being used by the work queue.
	 *
	 * @return number of worker threads
	 */
	public int size() {
		return workers.length;
	}


	public synchronized void increment() {
		pending++;
	}

	public synchronized void decrement() {
		pending--;

		if (pending == 0) {
			this.notifyAll();
			log.debug("Called notifyAll() that pending = {}", pending);
		}
	}

	/**
	 * Waits until work is available in the work queue. When work is found, will
	 * remove the work from the queue and run it. If a shutdown is detected,
	 * will exit instead of grabbing new work from the queue. These threads will
	 * continue running in the background until a shutdown is requested.
	 */
	private class PoolWorker extends Thread {

		@Override
		public void run() {
			Runnable r = null;

			outer:
			while (true) {
				synchronized (queue) {
					while (queue.isEmpty() && !shutdown) {
						if (waitingShutdown) {
							log.trace("Worker terminating.");
							break outer;
						}
						try {
							queue.wait();
						} catch (InterruptedException ex) {
							log.warn("Warning: Work queue interrupted.");
							Thread.currentThread().interrupt();
						}
					}
					// exit while for one of two reasons:
					// (a) queue has work, or (b) shutdown has been called

					if (shutdown) {
						log.warn("Worker forcefully terminated. pending = {}", pending);
						break;
					} else {
						r = queue.removeFirst();
						increment();
					}
				}

				try {
					r.run();
					decrement();
				} catch (RuntimeException ex) {
					// catch runtime exceptions to avoid leaking threads
					log.warn("Warning: Work queue encountered an exception while running.");
				}

			}

			log.trace("Worker at pending = {} ended", pending);
			if (pending == 0) {

				synchronized (this) {
					log.debug("Called this.notifyAll()");
					this.notifyAll();

				}
			}
		}
	}
}

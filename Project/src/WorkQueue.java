import java.util.LinkedList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A simple work queue implementation based on the IBM Developer article by
 * Brian Goetz. It is up to the user of this class to keep track of whether
 * there is any pending work remaining.
 *
 * @see <a href="https://www.ibm.com/developerworks/library/j-jtp0730/index.html">
 * Java Theory and Practice: Thread Pools and Work Queues</a>
 */
public class WorkQueue {
	/**
	 * A boolean which causes the calling thread to wait for the WorkQueue to finish.
	 */
	private volatile boolean waitingFinish;

	/**
	 * The count of workers still working. Synchronized by this object.
	 */
	private int pending;

	/**
	 * The logger of this class.
	 */
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
	private static final int DEFAULT = 5;

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
		this.queue = new LinkedList<>();
		this.workers = new PoolWorker[threads];

		this.shutdown = false;
		this.waitingFinish = false;
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
		synchronized (queue) {
			queue.addLast(r);
			queue.notifyAll();
		}
	}

	/**
	 * Waits for all pending work to be finished.
	 *
	 * @throws InterruptedException if wait() call gets interrupted.
	 */
	public void finish() throws InterruptedException {
		waitingFinish = true;

		synchronized (queue) {
			queue.notifyAll();
		}
		synchronized (this) {
			while (!queue.isEmpty() || pending > 0) {
				log.trace("finish() waiting at pending = {}, queue.size() = {}", pending, queue.size());
				this.wait();
				log.debug("finish() woke up with pending = {}.", pending);
			}
		}

		waitingFinish = false;
	}

	/**
	 * Asks the queue to shutdown. Any unprocessed work will not be finished,
	 * but threads in-progress will not be interrupted.
	 */
	public void shutdown() {
		// safe to do unsynchronized due to volatile keyword
		shutdown = true;
		log.debug("Shutting down...");
		synchronized (queue) {
			queue.notifyAll();
		}
		log.info("Work queue shutdown");
	}

	/**
	 * Returns the number of worker threads being used by the work queue.
	 *
	 * @return number of worker threads
	 */
	public int size() {
		return workers.length;
	}

	/**
	 * Increases pending by one. Synchronized by this object.
	 */
	private synchronized void increment() {
		pending++;
		log.trace("pending++ == {}", pending);
	}

	/**
	 * Decreases pending by one. Synchronized by this object.
	 */
	private synchronized void decrement() {
		pending--;
		log.trace("pending-- == {}", pending);
		if (pending == 0) {
			this.notifyAll();
			log.debug("Called notifyAll() in decrement(), pending = {}", pending);
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
			Runnable r;

			while (true) {
				synchronized (queue) {
					while (queue.isEmpty() && !shutdown) {
						if (waitingFinish) {
							synchronized (this) {
								this.notifyAll();
								log.debug("Called this.notifyAll()");
							}
						}
						try {
							log.debug("Waiting for work");
							queue.wait();
						} catch (InterruptedException ex) {
							log.warn("Warning: Work queue interrupted.");
							Thread.currentThread().interrupt();
						}
					}
					// exit while for one of two reasons:
					// (a) queue has work, or (b) shutdown has been called

					if (shutdown) {
						log.debug("Worker forcefully terminated.");
						break;
					} else {
						increment();
						r = queue.removeFirst();
						log.trace("Worker received work.");
					}
				}

				try {
					log.trace("Running...");
					r.run();
					log.trace("Running passed.");
					decrement();
				} catch (RuntimeException ex) {
					// catch runtime exceptions to avoid leaking threads
					log.warn("Warning: Running failed! Work queue encountered an exception while running. {}", ex.toString());
				}
			}

			log.debug("Worker at pending = {} ended", pending);
		}
	}
}

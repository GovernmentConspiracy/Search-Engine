import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ConcurrentModificationException;

/**
 * Maintains a pair of associated locks, one for read-only operations and one
 * for writing. The read lock may be held simultaneously by multiple reader
 * threads, so long as there are no writers. The write lock is exclusive, but
 * also tracks which thread holds the lock. If unlock is called by any other
 * thread, a {@link ConcurrentModificationException} is thrown.
 *
 * @see SimpleLock
 * @see SimpleReadWriteLock
 */
public class SimpleReadWriteLock {
	/**
	 * The logger of this class.
	 */
	private static final Logger log = LogManager.getLogger();

	/**
	 * int value representing the single active writer
	 */
	private static final int WRITING_STATE = -1;

	/**
	 * int value representing the dormant state
	 */
	private static final int DORMANT_STATE = 0; //READING_STATE is any value above DORMANT_STATE

	/**
	 * The lock used for reading.
	 */
	private final SimpleLock readerLock;

	/**
	 * The lock used for writing.
	 */
	private final SimpleLock writerLock;

	/**
	 * An int value representing the state of the lock:
	 * <p>
	 * -1 for 1 exclusive writer,
	 * 0 for no readers nor writers,
	 * >0 for lockState number of readers
	 */
	private int lockState;

	/**
	 * A simple lock of this class. Used for changing lockState.
	 */
	private final Object lock;

	/**
	 * Initializes a new simple read/write lock.
	 */
	public SimpleReadWriteLock() {
		readerLock = new ReadLock();
		writerLock = new WriteLock();

		lockState = 0;

		lock = new Object();
	}

	/**
	 * Returns the reader lock.
	 *
	 * @return the reader lock
	 */
	public SimpleLock readLock() {
		// NOTE: DO NOT MODIFY THIS METHOD
		return readerLock;
	}

	/**
	 * Returns the writer lock.
	 *
	 * @return the writer lock
	 */
	public SimpleLock writeLock() {
		// NOTE: DO NOT MODIFY THIS METHOD
		return writerLock;
	}

	/**
	 * Determines whether the thread running this code and the other thread are
	 * in fact the same thread.
	 *
	 * @param other the other thread to compare
	 * @return true if the thread running this code and the other thread are not
	 * null and have the same ID
	 * @see Thread#getId()
	 * @see Thread#currentThread()
	 */
	public static boolean sameThread(Thread other) {
		// NOTE: DO NOT MODIFY THIS METHOD
		return other != null && other.getId() == Thread.currentThread().getId();
	}

	/**
	 * Returns {@code true} if the lockState is dormant.
	 *
	 * @return returns {@code true} if the lockState is dormant.
	 */
	private boolean isDormant() {
		return lockState == DORMANT_STATE;
	}

	/**
	 * Returns {@code true} if the lockState is writing.
	 * Note: There can only be one writer.
	 *
	 * @return returns {@code true} if the lockState is writing.
	 */
	private boolean isWriting() {
		return lockState == WRITING_STATE;
	}

	/**
	 * Returns {@code true} if the lockState is reading.
	 * Note: There can be multiple readers.
	 *
	 * @return returns {@code true} if the lockState is reading.
	 */
	private boolean isReading() {
		return lockState > DORMANT_STATE;
	}

	/**
	 * Used to maintain simultaneous read operations.
	 */
	private class ReadLock implements SimpleLock {

		/**
		 * Will wait until there are no active writers in the system, and then will
		 * increase the number of active readers.
		 */
		@Override
		public void lock() {
			synchronized (lock) {
				while (isWriting()) { //While writer exists
					try {
						lock.wait();
					} catch (InterruptedException e) {
						log.warn("Experienced an InterruptedException");
					}
				}
				lockState++; //increase number of readers
			}
			log.trace("ReadLock locked.");
		}

		/**
		 * Will decrease the number of active readers, and notify any waiting threads if
		 * necessary.
		 */
		@Override
		public void unlock() {
			synchronized (lock) {
				if (!isReading()) { //Breaks if there's no reader threads
					throw new IllegalMonitorStateException();
				}
				lockState--; //decrease writer count
				if (isDormant()) { //Don't let reader's unlock() be annoying
					lock.notifyAll();
				}
			}
			log.trace("ReadLock Unlocked.");
		}
	}

	/**
	 * Used to maintain exclusive write operations.
	 */
	private class WriteLock implements SimpleLock {
		/**
		 * A parameter to keep track if the correct thread calls unlock().
		 */
		private Thread targetedThread = null; //Must sync write

		/**
		 * Will wait until there are no active readers or writers in the system, and
		 * then will increase the number of active writers and update which thread
		 * holds the write lock.
		 */
		@Override
		public void lock() {

			synchronized (lock) {
				while (!isDormant()) { //Needs permission in order to start writing
					try {
						lock.wait();
					} catch (InterruptedException e) {
						log.warn("Experienced an InterruptedException");
					}
				}
				lockState = WRITING_STATE;
				targetedThread = Thread.currentThread();
			}
			log.trace("WriteLock locked.");
		}

		/**
		 * Will decrease the number of active writers, and notify any waiting threads if
		 * necessary. If unlock is called by a thread that does not hold the lock, then
		 * a {@link ConcurrentModificationException} is thrown.
		 *
		 * @throws ConcurrentModificationException if unlock is called without previously
		 *                                         calling lock or if unlock is called by a thread that does not hold the write lock
		 * @see #sameThread(Thread)
		 */
		@Override
		public void unlock() throws ConcurrentModificationException {

			/*
			 * Should not call notifyAll() outside of synchronized block:
			 *  May cause fast bypass of two writer objects, one just entering the synchronized block,
			 *  the other notified in the while loop.
			 *  Meaning 2 concurrent writes
			 */
			synchronized (lock) {
				if (!isWriting() || !sameThread(targetedThread)) {
					throw new ConcurrentModificationException();
				}

				lockState = DORMANT_STATE;
				targetedThread = null;
				lock.notifyAll();
			}
			log.trace("WriteLock unlocked.");
		}

	}

}

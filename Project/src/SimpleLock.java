import java.util.concurrent.locks.Lock;

/**
 * A simple lock used for conditional synchronization as an alternative to using
 * a {@code synchronized} block.
 * <p>
 * Similar but simpler than {@link Lock}.
 */
public interface SimpleLock {

	// NOTE: DO NOT MODIFY THIS CLASS

	/**
	 * Acquires the lock. If the lock is not available then the current thread
	 * becomes disabled for thread scheduling purposes and lies dormant until the
	 * lock has been acquired.
	 */
	void lock();

	/**
	 * Releases the lock.
	 */
	void unlock();

}

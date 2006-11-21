package net.link.safeonline;

/**
 * Components implementing this interface can be notified during application
 * startup and shutdown.
 * 
 * @author fcorneli
 * 
 */
public interface Startable {

	void start();

	void stop();
}

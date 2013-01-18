// ----------------------------------------------------------------------
// This code is developed as part of the Java CoG Kit project
// The terms of the license can be found at http://www.cogkit.org/license
// This message may not be removed or altered.
// ----------------------------------------------------------------------

package org.globus.cog.util.timer;

import java.util.Enumeration;
import java.util.Vector;

import org.globus.cog.util.ConditionVariable;

public class Timer implements Runnable {
  long durationMillis;
  long startTime;
  long stopTime;
  Vector listeners = new Vector( 1 );
  ConditionVariable condVar = new ConditionVariable();

  public Timer() {
    this( 0 );
  }

  public Timer( long millis ) {
    setDuration( millis );
  }

  public void addTimerListener( TimerListener tl ) {
    listeners.add( tl );
  }

  public void removeTimerListener( TimerListener tl ) {
    listeners.remove( tl );
  }

  private void fireTimerStarted() {
    startTime = System.currentTimeMillis();

    Enumeration tlEnum = listeners.elements();
    while( tlEnum.hasMoreElements() ) {
      ((TimerListener)tlEnum.nextElement()).timerStarted( this );
    }
  }

  private void fireTimerFinished() {
    stopTime = System.currentTimeMillis();

    Enumeration tlEnum = listeners.elements();
    while( tlEnum.hasMoreElements() ) {
      ((TimerListener)tlEnum.nextElement()).timerFinished( this );
    }
  }

  public void setDuration( long millis ) {
    durationMillis = millis;
  }

  public long getDuration() {
    return durationMillis;
  }

  public long getStartTime() {
    return startTime;
  }

  public long getStopTime() {
    return stopTime;
  }

  public long getElapsedTime() {
    return stopTime - startTime;
  }

  public void run() {
    this.fireTimerStarted();

    synchronized( condVar ) {
      try {
	if( durationMillis == 0 ) {
	  condVar.wait();
	} else {
	  condVar.wait( durationMillis );
	}
      } catch( java.lang.InterruptedException ie ) {
	System.out.println( "Fatal Error: timer interrupted--times invalid" );
	return;
      }
    }

    this.fireTimerFinished();
  }

  public final void start() {
    synchronized ( condVar ) {
      condVar.setValue( 1 );
      new Thread( this ).start();
    }
  }

  public final void stop() {
    synchronized ( condVar ) {
      if( condVar.getValue() == 1 ) {
	condVar.notifyAll();
	condVar.setValue( 0 );
      }
    }
  }
}

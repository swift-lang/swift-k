package org.globus.transfer.reliable.client.credential;

public class ProxyInfo {
	private String subject;
	private int strength;
	private String timeLeft;

	public ProxyInfo(String subject, int strength, String timeLeft) {
		super();
		this.subject = subject;
		this.strength = strength;
		this.timeLeft = timeLeft;
	}
	
	public String getSubject() {
		return subject;
	}

	public int getStrength() {
		return strength;
	}

	public String getTimeLeft() {
		return timeLeft;
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("Subject: ").append(subject).append("\n")
		   .append("Strength: ").append(strength).append(" bits\n")
		   .append("Time Left: ").append(timeLeft);
		
		return buf.toString();
	}

}

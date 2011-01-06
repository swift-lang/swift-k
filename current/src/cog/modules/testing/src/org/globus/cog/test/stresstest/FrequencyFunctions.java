package org.globus.cog.test.stresstest;

import java.util.Random;

abstract class FrequencyFunction
{
    protected long millisecondsTillNext;
    protected long originalDelay;

    public FrequencyFunction(int seconds) {
	originalDelay = millisecondsTillNext = seconds*1000;
    }
    
    public void reset() {
	millisecondsTillNext = originalDelay;
    }
    //Override me:
    public abstract long nextDelay(); //milliseconds
}

abstract class UsersFunction
{
    public abstract int getNumber();
}

class ConstantFrequency extends FrequencyFunction
{
    public ConstantFrequency(int seconds) { super(seconds);}
    public long nextDelay() {
	return millisecondsTillNext;
    }
}

class ExponentialFrequency extends FrequencyFunction
{
    double factor;

    public ExponentialFrequency(int seconds, double factor) {
	super(seconds);
	this.factor = factor;
    }

    public ExponentialFrequency() {
	super(3600);
	this.factor = 2;
	//Default: Start once an hour and double frequency
    }

    public long nextDelay() {
	if (millisecondsTillNext > 1000) //minimum delay = 1 second
	    millisecondsTillNext = Math.round(millisecondsTillNext/factor) ;
	return millisecondsTillNext;
    }
    //call reset() on this to return to original delay length
}

class RandomFrequency extends FrequencyFunction
{
    Random someRandomness;
    int standardDev;
    
    public RandomFrequency(int meanSeconds, int standardDev) {
	super(meanSeconds);
	someRandomness = new Random();
	this.standardDev = standardDev*1000; //convert to milliseconds
    }

    public long nextDelay() {
	long result;
	result =  millisecondsTillNext+
	    Math.round(someRandomness.nextGaussian()*standardDev);
	return result>0?result:0;
    }
}

class ConstantUsers extends UsersFunction
{
    int howMany;

    public ConstantUsers(int x) {
	howMany = x;
    }

    public int getNumber() {
	return howMany;
    }
}

class ExponentialUsers extends UsersFunction
{
/*Dangerous!  Will keep doubling number of users, which means doubling the
number of threads, until JVM runs out of memory.  I don't recommend actually
using ExponentialUsers.*/
    int factor;
    int baseNum;

    public ExponentialUsers(int baseNum, int factor) {
	this.baseNum = baseNum;
	this.factor = factor;
    }

    public ExponentialUsers() {
	this.baseNum = 1;
	this.factor = 2;
	//Default: Start with one user and double
    }

    public int getNumber() {
	baseNum *= factor;
	return baseNum;
    }
}

class LinearUsers extends UsersFunction
{
    int factor;
    int baseNum;

    public LinearUsers(int baseNum, int factor) {
	this.baseNum = baseNum;
	this.factor = factor;
    }

    public LinearUsers() {
	this.baseNum = 1;
	this.factor = 1;
	//Default: Start with one user and add one periodically
    }

    public int getNumber() {
	baseNum += factor;
	return baseNum;
    }
}

class RandomUsers extends UsersFunction
{
    Random someRandomness;
    int standardDev;
    int meanUsers;
    
    public RandomUsers(int meanUsers, int standardDev) {
	someRandomness = new Random();
	this.meanUsers = meanUsers;
	this.standardDev = standardDev; //convert to milliseconds
    }

    public int getNumber() {
	int result;
	result = meanUsers+
	    (int)Math.round(someRandomness.nextGaussian()*standardDev);
	
	return result<0?0:result;
    }
}
//exponential users, random users, linear users, linear frequency...

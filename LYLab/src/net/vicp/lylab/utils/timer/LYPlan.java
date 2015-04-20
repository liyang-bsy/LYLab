package net.vicp.lylab.utils.timer;

import java.util.Timer;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 * 	LYPlan is a tiny schedule framework, could be apply to multitude purpose.<br>
 * 
 * 	<br>Release Under GNU Lesser General Public License (LGPL).
 * 
 * @author Young Lee
 * @since 2014.5.21
 * @version 1.0.0
 * 
 */
public class LYPlan implements ApplicationListener {

	private TimerJob[] jobs = null;
	
	private boolean Scheduled = false;
	private static Boolean inited = false;
	private static LYPlan self = null;

	@Override
	public void onApplicationEvent(ApplicationEvent arg) {
		if(!inited)
		{
			inited = true;
			System.out.println("LYPlan - Initialization started");
			
			self = this;
			this.BeginSchedule();
		}
	}
	
	public void BeginSchedule() {
		if (Scheduled)
			return;
		else
			Scheduled = true;
		Timer timer = new Timer();
		for(TimerJob bj:this.getJobs())
		{
			if(bj.getInterval()!=0)
				timer.schedule(bj, bj.getStartTime(), bj.getInterval());
			else
				timer.schedule(bj, bj.getStartTime());
			System.out.println("LYPlan - Load scheduled job: " + bj.getClass().getName());
		}
	}

	/**
	 * TimeJob will be cancelled before start(if possible).
	 * @param bj	TimeJob you want to start
	 * @return
	 */
	public boolean NewTimeJob(TimerJob bj)
	{
		try{
			bj.cancel();
			Timer timer = new Timer();
			if(bj.getInterval()!=0)
				timer.schedule(bj, bj.getStartTime(), bj.getInterval());
			else
				timer.schedule(bj, bj.getStartTime());
			System.out.println("LYPlan - Load new schedule job: " + bj.getClass().getName());
		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public TimerJob[] getJobs() {
		return jobs;
	}
	
	public void setJobs(TimerJob[] jobs) {
		this.jobs = jobs;
	}

	public static LYPlan getSelf() {
		return self;
	}

	public static void setSelf(LYPlan self) {
		LYPlan.self = self;
	}
	
}

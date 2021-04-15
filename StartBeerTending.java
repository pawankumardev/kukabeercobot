package application;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import com.kuka.common.ThreadUtil;
import com.kuka.generated.ioAccess.EK_IOIOGroup;
import com.kuka.generated.ioAccess.MediaFlangeIOGroup;
import com.kuka.roboticsAPI.applicationModel.RoboticsAPIApplication;
import static com.kuka.roboticsAPI.motionModel.BasicMotions.*;

import com.kuka.roboticsAPI.controllerModel.Controller;
import com.kuka.roboticsAPI.deviceModel.LBR;
import com.kuka.roboticsAPI.geometricModel.CartDOF;
import com.kuka.roboticsAPI.geometricModel.CartPlane;
import com.kuka.roboticsAPI.geometricModel.ObjectFrame;
import com.kuka.roboticsAPI.geometricModel.Tool;
import com.kuka.roboticsAPI.geometricModel.math.Vector;
import com.kuka.roboticsAPI.motionModel.MotionBatch;
import com.kuka.roboticsAPI.motionModel.controlModeModel.CartesianImpedanceControlMode;
import com.kuka.roboticsAPI.motionModel.controlModeModel.CartesianSineImpedanceControlMode;
import com.kuka.roboticsAPI.sensorModel.ForceSensorData;

/**
 * Implementation of a robot application.
 * <p>
 * The application provides a {@link RoboticsAPITask#initialize()} and a
 * {@link RoboticsAPITask#run()} method, which will be called successively in
 * the application lifecycle. The application will terminate automatically after
 * the {@link RoboticsAPITask#run()} method has finished or after stopping the
 * task. The {@link RoboticsAPITask#dispose()} method will be called, even if an
 * exception is thrown during initialization or run.
 * <p>
 * <b>It is imperative to call <code>super.dispose()</code> when overriding the
 * {@link RoboticsAPITask#dispose()} method.</b>
 * 
 * @see UseRoboticsAPIContext
 * @see #initialize()
 * @see #run()
 * @see #dispose()
 */
public class StartBeerTending extends RoboticsAPIApplication {
	@Inject
	private LBR robot;
	private Controller cab;
	
	private double jointVel;
	private double cont1;
	private double cont2;
	private double pathVel;
	private double pathVel2;
	EK_IOIOGroup EK_IO;
	MediaFlangeIOGroup Media_IO;
	private Tool myTool;
	private ObjectFrame tcp;

	@Override
	public void initialize() {
		// initialize your application here
		cab = getController("KUKA_Sunrise_Cabinet_1");
		robot = (LBR) getDevice(cab, "LBR_iiwa_14_R820_1");

		jointVel = getApplicationData().getProcessData("JointVel").getValue();
		
		pathVel = getApplicationData().getProcessData("PathVel").getValue();
		
		cont1 = getApplicationData().getProcessData("cont1").getValue();
		
		cont2 = getApplicationData().getProcessData("cont2").getValue();
		
		pathVel2 = getApplicationData().getProcessData("PathVel2").getValue();

		myTool = (Tool) getApplicationData().createFromTemplate(
				"TCP_Bottle_Neck");

		myTool.attachTo(robot.getFlange());
		
		tcp = myTool.getFrame("TCP");

		EK_IO = new EK_IOIOGroup(cab);
		Media_IO = new MediaFlangeIOGroup(cab);

	}

	@Override
	public void run() {
		// your application execution starts here

		getLogger().info("Goining to Home");

		robot.move(ptp(getApplicationData().getFrame("/Home"))
				.setJointVelocityRel(jointVel));// Home

		getLogger().info("HomeReached");

		OpenGripper();

		pickPlaceGlass();

		pickPour();
		cheersBeer();

	}

	public void OpenGripper() {
		ThreadUtil.milliSleep(1);
		EK_IO.setCloseGripper(false);
		EK_IO.setOpenGripper(true);
		ThreadUtil.milliSleep(1000);

	}

	public void CloseGripper() {

		ThreadUtil.milliSleep(1);
		EK_IO.setOpenGripper(false);
		EK_IO.setCloseGripper(true);

		ThreadUtil.milliSleep(1000);

	}

	public void pickPlaceGlass() {

		robot.moveAsync(ptp(getApplicationData().getFrame("/GlassPrePick1"))
				.setJointVelocityRel(jointVel).setBlendingCart(cont1));
		
		robot.moveAsync(lin(getApplicationData().getFrame("/GlassPrePick"))
				.setCartVelocity(pathVel).setBlendingCart(cont1));

		

		robot.move(lin(getApplicationData().getFrame("/GlassPick"))
				.setCartVelocity(pathVel));

		// Close Gripper

		CloseGripper();

		

		robot.moveAsync(lin(getApplicationData().getFrame("/P1")).setCartVelocity(pathVel).setBlendingCart(cont1));

		

		robot.moveAsync(ptp(getApplicationData().getFrame("/P2"))
				.setJointVelocityRel(jointVel).setBlendingCart(cont1));

		robot.move(lin(getApplicationData().getFrame("/P3"))
				.setCartVelocity(pathVel));

		// open Gripper

		OpenGripper();

		robot.moveAsync(lin(getApplicationData().getFrame("/P2"))
				.setCartVelocity(pathVel).setBlendingCart(cont1));

		/*
		 * 
		 * 
		 * 
		 * robot.move(lin(getApplicationData().getFrame("/P10")));
		 * 
		 * robot.move(lin(getApplicationData().getFrame("/P10")));
		 * 
		 * robot.move(lin(getApplicationData().getFrame("/P10")));
		 * 
		 * robot.move(lin(getApplicationData().getFrame("/P10")));
		 * 
		 * robot.move(lin(getApplicationData().getFrame("/P10")));
		 * 
		 * robot.move(lin(getApplicationData().getFrame("/P10")));
		 */

	}

	public void pickPour() {

		robot.moveAsync(ptp(getApplicationData().getFrame("/BottlePrePick"))
				.setJointVelocityRel(jointVel).setBlendingCart(cont1));

		robot.move(lin(getApplicationData().getFrame("/BottlePick"))
				.setCartVelocity(pathVel));

		// Close Gripper
		CloseGripper();

		robot.moveAsync(lin(getApplicationData().getFrame("/BottlePrePick"))
				.setCartVelocity(pathVel).setBlendingCart(cont1));

	
		// Air Points

		//Use TCP

		tcp.moveAsync(ptp(getApplicationData().getFrame("/Opener2"))
				.setJointVelocityRel(jointVel).setBlendingCart(cont1));

		// robot.moveAsync(ptp(getApplicationData().getFrame("/afterhelp")).setJointVelocityRel(0.2).setBlendingCart(20));

		// Opener stand

		tcp.moveAsync(lin(getApplicationData().getFrame("/Opener1"))
				.setCartVelocity(pathVel).setBlendingCart(cont1));

		tcp.moveAsync(lin(getApplicationData().getFrame("/Opener3")).setCartVelocity(
				pathVel).setBlendingCart(cont1));

		tcp.move(lin(getApplicationData().getFrame("/P4")).setCartVelocity(
				pathVel));

		tcp.move(lin(getApplicationData().getFrame("/P5")).setCartVelocity(
				2000));

		tcp.move(lin(getApplicationData().getFrame("/P6")).setCartVelocity(
				2000));

		tcp.moveAsync(ptp(getApplicationData().getFrame("/P9")).setJointVelocityRel(jointVel).setBlendingCart(cont1));

		tcp.moveAsync(lin(getApplicationData().getFrame("/P7")).setCartVelocity(
				100).setBlendingCart(cont1));
		
		
		
		CartesianImpedanceControlMode comMode = new CartesianImpedanceControlMode();

		comMode.parametrize(CartDOF.X).setStiffness(2500);
		comMode.parametrize(CartDOF.Y).setStiffness(2500);
		comMode.parametrize(CartDOF.Z).setStiffness(2500);
		comMode.parametrize(CartDOF.ALL).setDamping(1);
		// Out of Opener Stand

		tcp.move(lin(getApplicationData().getFrame("/P8"))
				.setJointVelocityRel(0.05).setCartVelocity(10).setMode(comMode));//Pouring Pos

		ThreadUtil.milliSleep(3000);
		
		
		 tcp.move(lin(getApplicationData().getFrame("/P10")).setCartVelocity(pathVel2));
		

		tcp.moveAsync(lin(getApplicationData().getFrame("/P11")).setBlendingCart(cont2)
				.setCartVelocity(pathVel2));

		tcp.moveAsync(lin(getApplicationData().getFrame("/P12")).setBlendingCart(cont2)
				.setCartVelocity(pathVel2));

		tcp.moveAsync(lin(getApplicationData().getFrame("/P13")).setBlendingCart(cont2)
				.setJointVelocityRel(0.2).setCartVelocity(pathVel2));

		tcp.moveAsync(lin(getApplicationData().getFrame("/P14")).setCartVelocity(pathVel2).setBlendingCart(cont2)
			);
		
		tcp.moveAsync(lin(getApplicationData().getFrame("/P15")).setCartVelocity(pathVel2).setBlendingCart(cont2)
				);
		
		
		///Going for Shaking
		
		
		tcp.moveAsync(lin(getApplicationData().getFrame("/P23")).setCartVelocity(pathVel).setBlendingCart(cont2)
				);
		
		
		// Wiggle Bounce

			

				CartesianSineImpedanceControlMode shakeSinA;
				shakeSinA = CartesianSineImpedanceControlMode.createSinePattern(
						CartDOF.A, 5, 5, 15);
				shakeSinA.parametrize(CartDOF.ALL).setDamping(0.7);

				robot.moveAsync(positionHold(shakeSinA, 3, TimeUnit.SECONDS));

				CartesianSineImpedanceControlMode shakeSpirale;
				shakeSpirale = CartesianSineImpedanceControlMode.createSpiralPattern(
						CartPlane.XY, 2, 16, 1000, 180);
				shakeSpirale.setRiseTime(0.2).setHoldTime(60).setFallTime(0.5);

				robot.moveAsync(positionHold(shakeSpirale, 3, TimeUnit.SECONDS));

				ThreadUtil.milliSleep(10);

		
		
		
				tcp.moveAsync(lin(getApplicationData().getFrame("/P24")).setCartVelocity(1000).setBlendingCart(cont2)
						);
		
				tcp.moveAsync(lin(getApplicationData().getFrame("/P25")).setCartVelocity(pathVel2).setBlendingCart(cont2)
						);
		
		
				tcp.moveAsync(lin(getApplicationData().getFrame("/P26")).setCartVelocity(2000).setBlendingCart(cont2)
						);
		
				tcp.moveAsync(lin(getApplicationData().getFrame("/P25")).setCartVelocity(2000).setBlendingCart(cont2)
						);
		
		
				tcp.moveAsync(lin(getApplicationData().getFrame("/P26")).setCartVelocity(2000).setBlendingCart(cont2)
						);
		
				tcp.moveAsync(lin(getApplicationData().getFrame("/P25")).setCartVelocity(2000).setBlendingCart(cont2)
						);
		
		
				tcp.move(lin(getApplicationData().getFrame("/P26")).setCartVelocity(2000).setBlendingCart(cont2)
						);
		ThreadUtil.milliSleep(1000);
		
		tcp.moveAsync(lin(getApplicationData().getFrame("/P25")).setCartVelocity(pathVel).setBlendingCart(cont2)
				);
		
		tcp.moveAsync(ptp(getApplicationData().getFrame("/P16")).setJointVelocityRel(jointVel).setBlendingCart(cont2)
				);
		
		
		tcp.moveAsync(lin(getApplicationData().getFrame("/P17")).setJointVelocityRel(jointVel).setBlendingCart(cont2)
				);
		
		tcp.move(lin(getApplicationData().getFrame("/P18")).setCartVelocity(100)
				);
		
		OpenGripper();
		tcp.moveAsync(lin(getApplicationData().getFrame("/P17")).setJointVelocityRel(jointVel).setBlendingCart(cont2)
				);
		
		
		
		
	

		
	}

	public void cheersBeer() {

		tcp.moveAsync(ptp(getApplicationData().getFrame("/P27"))
				.setJointVelocityRel(jointVel).setBlendingCart(cont1));

		tcp.moveAsync(lin(getApplicationData().getFrame("/P19")).setCartVelocity(pathVel)
				.setBlendingCart(cont1));
		tcp.move(lin(getApplicationData().getFrame("/P20"))
				.setCartVelocity(pathVel));
		// Close Gripper

		CloseGripper();

		tcp.move(lin(getApplicationData().getFrame("/P21")).setCartVelocity(
				pathVel));

		tcp.move(lin(getApplicationData().getFrame("/P22"))
				.setCartVelocity(pathVel).setBlendingCart(cont1));

		while (true) {

			ForceSensorData data = robot.getExternalForceTorque(robot
					.getFlange());
			Vector force = data.getForce();

			double forceInX = force.getZ();
			if (forceInX < 0) {
				forceInX = -(forceInX);

			}

			if (forceInX > 20.0)

			{
				getLogger().info("Cheers Triggered");

				ThreadUtil.milliSleep(50);

				OpenGripper();

				break;

			}

		}

		ThreadUtil.milliSleep(5000);
		getLogger().info("Near Home");

		robot.move(ptp(getApplicationData().getFrame("/Home"))
				.setJointVelocityRel(jointVel));// Home

		getLogger().info("Near Home");

	}

}
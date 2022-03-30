// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

// this is the package that allows us to use the things from FIRST, it has to be the first thing in the program
package frc.robot;


// these are the imports which allow us to use certain commands and classes
import javax.management.BadBinaryOpValueExpException;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.revrobotics.CANSparkMax;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.motorcontrol.*;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.drive.MecanumDrive;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.SparkMaxPIDController;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import edu.wpi.first.cameraserver.CameraServer;


// this is where we define our controllers, motors, and other things
public class Robot extends TimedRobot {

  Command m_autonomousCommand;
  SendableChooser<Command> m_chooser = new SendableChooser<>();
  
  Timer timer;

  MecanumDrive robotDrive;

  PWMVictorSPX frontLeft, frontRight, backLeft, backRight;

  XboxController xbox1;
  XboxController xbox2;
  
  PWMVictorSPX shooter;
  PWMVictorSPX belt;

  PWMVictorSPX intakeMotor;

  PWMVictorSPX climberOne;
  PWMVictorSPX climberTwo;

  //orientation
  boolean orientation = true;


  // this is the robotInit, which initializes things for us on the robot
  @Override
  public void robotInit() {
    // Define the different controllers as seperate and distinct inputs
    xbox1 = new XboxController(Constants.xboxController1);
    xbox2 = new XboxController(Constants.xboxController2);
    
    // Define the motors to the robot as a thing on the robot
    frontLeft = new PWMVictorSPX(Constants.leftFrontMotor);
    frontRight = new PWMVictorSPX(Constants.rightFrontMotor);
    backLeft = new PWMVictorSPX(Constants.leftBackMotor);
    backRight = new PWMVictorSPX(Constants.rightBackMotor);
    frontRight.setInverted(true);
    backRight.setInverted(true);
    
    // Defines the shooter to the robot as a motor
    shooter = new PWMVictorSPX(Constants.ShooterMotor);

    // Defines the belt to the robot as a motor
    belt = new PWMVictorSPX(Constants.beltMotor);
        
    // Defines the intake motor to the robot as a motor
    intakeMotor = new PWMVictorSPX(Constants.intakeMotor);

    // Defines the climbing system to the robot as a motor
    climberOne = new PWMVictorSPX(Constants.ClimberMotorOne);
    climberTwo = new PWMVictorSPX(Constants.ClimberMotorTwo);

    // Tells the robot that we are using a mechanum system for driving 
    robotDrive = new MecanumDrive(frontLeft, backLeft, frontRight, backRight);

    // Code for the camera that you can attach to the robot, this should be all you need for the camera
    CameraServer.startAutomaticCapture();

  }

  // this is the code for the teleop period where we are in control of the robot
  @Override
  public void teleopPeriodic() {

    robotDrive.setSafetyEnabled(false);
    double speedCap = .75;
    double spinCap = .5;

    // this code is what allows the A and B buttons to switch the orientation of the robot (aka which way it thinks is its front) and allows it to drive
    if(orientation)
    {
      robotDrive.driveCartesian(-speedCap*xbox2.getRawAxis(1), speedCap*xbox2.getRawAxis(0), spinCap*xbox2.getRawAxis(4));
    }
    else
    {
      robotDrive.driveCartesian(speedCap*xbox2.getRawAxis(1), -speedCap*xbox2.getRawAxis(0), spinCap*xbox2.getRawAxis(4));
    }

    // xbox controller 2 button A (driver controller) reverses orientation
    if (xbox2.getAButton())
    {
       orientation = false;
    }
    // xbox controller 2 button B (driver controller) sets orientation to normal
    if (xbox2.getBButton())
    {
       orientation = true;
    }

    //xbox controller 2 (driver controller) turns the intake on if X is pressed
    if(xbox2.getXButton())
    {
      intakeMotor.set(-1.00);
    }
    //xbox controller 2 (driver controller) turns the intake backwards if Y is pressed
    else if(xbox2.getYButton()) 
    {
      intakeMotor.set(1.00); 
    }
    //Stops the motor if nothing is being pressed
    else
    {
      intakeMotor.stopMotor();
    }

    // xbox controller 1 (other controller) B button shoots
    if (xbox1.getBButton())
    {
       shooter.set(1.00);    
    }
    // xbox controller 1 (other controller) A button reverses shooter
    else if(xbox1.getAButton())
    {
      shooter.set(-1.00);
    }
    // stops the motor if nothing is being pressed
    else 
    {
      shooter.stopMotor();
    }

    // Xbox controller 1 (other controller) X button moves the belt down
    if (xbox1.getXButton())
    {
      belt.set(-1.00);  
    }
    // xbox controller 1 (other controller) Y button moves the belt up
    else if(xbox1.getYButton())
    {
      belt.set(1.00);
    }
    //stops the motor if nothing is being pressed
    else
    {
      belt.stopMotor();
    }
    
    //xbox controller 1 (other controller) deploys the climbers if the left bumper is pressed 
    if(xbox1.getLeftBumper())
    {
      climberOne.set(1.00);
      climberTwo.set(1.00);
    }
    //this reverses the climbers so we can bring them back down
    else if(xbox1.getRightBumper())
    {
      climberOne.set(-1.00);
      climberTwo.set(-1.00);
    }
    //stops the motors if nothing is being pressed
    else
    {
      climberOne.stopMotor();
      climberTwo.stopMotor();
    }

  }
  //This is our autonomous code, which is when the robot is not being driven by humans
  @Override
  public void autonomousInit() {
    //m_autonomousCommand = m_chooser.getSelected();

    robotDrive.setSafetyEnabled(false);
    timer = new Timer();
    timer.reset();
    timer.start();

    //Declaring variables and arrays
    double[] xSpeed = {0.4, 0, 0, 0};
    double[] ySpeed = {0, 0, 0, 0};
    double[] zSpeed = {0, 0, 0.1, 0};
    double[] timeIntevals = {4, 0, 0, 0};
    //double[] alecBaldwin = {0, 0, 0, 0};
    double autonTime;
 
    //Main for loop
    for(int i = 0; i < 4; i++){
      
      //Sets the current time to autonTime
      autonTime = timer.get();
      
      /*if(alecBaldwin[i] == 1){
        
        shooterLeft.set(1);
        shooterRight.set(-1);
        /*
        while(timer.get() < autonTime + 0.5){
          intake1.set(1.0);
          intake2.set(1.0);
        }

        while(timer.get() < autonTime + 3){
          intake1.set(-1.0);
          intake2.set(-1.0);
        }

        shooterLeft.stopMotor();


        shooterRight.stopMotor();
      }*/

      //Runs in between the time intervals
      while(timer.get() < autonTime + timeIntevals[i]){

        //Drives the robot
        robotDrive.driveCartesian(xSpeed[i], ySpeed[i], zSpeed[i]);
  
      }
      //Stops the motors
      shooter.stopMotor();
      belt.stopMotor();
      intakeMotor.stopMotor();
      climberOne.stopMotor();
      climberTwo.stopMotor();
    }

    robotDrive.driveCartesian(0, 0, 0);

    // schedule the autonomous command (example)
    /*if (m_autonomousCommand != null) {
      m_autonomousCommand.schedule();
    }*/
  }

  @Override
  public void autonomousPeriodic() {
    /*
    robotDrive.driveCartesian(0, -0.25, 0);
    Timer.delay(1);
    robotDrive.driveCartesian(0, 0, 0);
    /*
    /*
     * String autoSelected = SmartDashboard.getString("Auto Selector",
     * "Default"); switch(autoSelected) { case "My Auto": autonomousCommand
     * = new MyAutoCommand(); break; case "Default Auto": default:
     * autonomousCommand = new ExampleCommand(); break; }
     */

  }

}

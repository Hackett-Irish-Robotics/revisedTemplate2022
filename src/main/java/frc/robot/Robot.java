// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import javax.management.BadBinaryOpValueExpException;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.motorcontrol.*;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.drive.MecanumDrive;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;

/**
 * This is a demo program showing the use of the DifferentialDrive class, specifically it contains
 * the code necessary to operate a robot with tank drive.
 */
public class Robot extends TimedRobot {

  Command m_autonomousCommand;
  SendableChooser<Command> m_chooser = new SendableChooser<>();
  
  Timer timer;

  MecanumDrive robotDrive;

  PWMVictorSPX frontLeft, frontRight, backLeft, backRight;

  XboxController xbox1;
  XboxController xbox2;
  
  PWMVictorSPX shooterLeft;
  PWMVictorSPX shooterRight;

  PWMVictorSPX intake;

  @Override
  public void robotInit() {
    //Define the different controllers as seperate and distinct inputs
    xbox1 = new XboxController(Constants.xboxController1);
    xbox2 = new XboxController(Constants.xboxController2);
    
    //Define the motors to the robot as a thing on the robot
    frontLeft = new PWMVictorSPX(Constants.leftFrontMotor);
    frontRight = new PWMVictorSPX(Constants.rightFrontMotor);
    backLeft = new PWMVictorSPX(Constants.leftBackMotor);
    backRight = new PWMVictorSPX(Constants.rightBackMotor);
    frontRight.setInverted(true);
    backRight.setInverted(true);
    
    //Define the shooters to the robot as motors
    shooterLeft = new PWMVictorSPX(Constants.leftShooterMotor);
    shooterRight = new PWMVictorSPX(Constants.rightShooterMotor);
        
    //Define the intake
    intake = new PWMVictorSPX(Constants.intakeMotor);

    //Tells the robot that we are using a mechanum system
    robotDrive = new MecanumDrive(frontLeft, backLeft, frontRight, backRight);
  }

  @Override
  public void teleopPeriodic() {

    robotDrive.setSafetyEnabled(false);
    double speedCap = .25;
    double spinCap = .69;
    robotDrive.driveCartesian(-speedCap*xbox2.getRawAxis(1), speedCap*xbox2.getRawAxis(0), speedCap*xbox2.getRawAxis(4));


    // xbox controller A button shoots
    if (xbox1.getAButton())
    {
       shooterLeft.set(-0.2);    
       shooterRight.set(0.2);
    }
    // Xbox controller B Button reverses shooter (in case ball gets stuck in intake)
    else if (xbox1.getBButton())
    {
      shooterLeft.set(1);  
      shooterRight.set(-1);
    }
    // Else the shooter motors stop
    else
    {
      shooterLeft.stopMotor();
      shooterRight.stopMotor();
    }

    /*Turns the intake on if X is pressed
    if(xbox1.getXButton())
    {
      intake.set(-1);
    }
    //Turns the intake backwards if Y is pressed
    else if(xbox1.getYButton())
    {
      intake.set(0.25);
    }

    //Stops the motor is nothin is bein pressed
    else
    {
      intake.stopMotor();
    }
    */

    //Turns the intake on if X is pressed
    if(xbox1.getXButton())
    {
      double intakeTime = timer.get();

      while(timer.get() < intakeTime + 3){
        intake.set(-1);
      }
    }
    //Turns the intake backwards if Y is pressed
    else if(xbox1.getYButton())
    {
      double intakeTime = timer.get();

      while(timer.get() < intakeTime + 0.25){
        intake.set(1);
      }
      
      intakeTime = timer.get();

      while(timer.get() < intakeTime + 2){
        intake.set(-0.5);
      }
    }
    //Stops the motor is nothin is bein pressed
    else
    {
      intake.stopMotor();
    }

  }

  @Override
  public void autonomousInit() {
    //m_autonomousCommand = m_chooser.getSelected();

    robotDrive.setSafetyEnabled(false);
    timer = new Timer();
    timer.reset();
    timer.start();

    //Declaring variabled and arrays
    double[] xSpeed = {-0.5, 0.5, 0, 0};
    double[] ySpeed = {0, 0, 0, 0};
    double[] zSpeed = {0, 0, 0.1, 0};
    double[] timeIntevals = {1.5, 0.3, 0.5, 3};
    double[] alecBaldwin = {0, 0, 0, 1};
    double autonTime;
 
    //Main for loop
    for(int i = 0; i < 4; i++){

      //Sets the current time to autonTime
      autonTime = timer.get();

      //Runs in between the time intervals
      while(timer.get() < autonTime + timeIntevals[i]){

        //Drives the robot
        robotDrive.driveCartesian(xSpeed[i], ySpeed[i], zSpeed[i]);
      
        //Runs the shooters if they need to be
        shooterLeft.set(alecBaldwin[i]);
        shooterRight.set(-alecBaldwin[i]);
      }
      //Stops the motors
      shooterLeft.stopMotor();
      shooterRight.stopMotor();
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

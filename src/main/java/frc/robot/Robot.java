// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import javax.management.BadBinaryOpValueExpException;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.WPI_TalonSRX;
import com.revrobotics.CANSparkMax;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.PneumaticsModuleType;
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
//import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Solenoid;

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

  PWMVictorSPX intakeMotor;


  /* 
  public WPI_TalonSRX intakeDeploy;
  private double intakeDeployPos = 0;
  private final double INTAKE_DOWN = 2269;
  private final double INTAKE_UP = 0; 
  */

  //CANSparkMax intake2;

  //orientation
  boolean orientation = true;

  //pneumatics
  //DoubleSolenoid m_doubleSolenoid;
  //Solenoid pneumaticClimber;


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
    intakeMotor = new PWMVictorSPX(Constants.intakeMotor);
    //intakeDeploy = new WPI_TalonSRX(Constants.intakeMotor);
    //intakeDeploy.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder);
    //intakeDeploy.setSelectedSensorPosition(0);

    //Tells the robot that we are using a mechanum system
    robotDrive = new MecanumDrive(frontLeft, backLeft, frontRight, backRight);

    // Need to see with the camera.
    // Discussions online note that there is no way
    // to rotate it in Java. Might be able to
    // change orientation in the driver station.
    // Must be rotated at driver station.
    CameraServer.startAutomaticCapture();

   //pneumatics attempt number one
   // DoubleSolenoid corresponds to a double solenoid.
    //m_doubleSolenoid = new DoubleSolenoid(PneumaticsModuleType.CTREPCM, 0, 1);
    //pneumaticClimber = new Solenoid(PneumaticsModuleType.CTREPCM, Constants.pneumaticChannel);
  }

  @Override
  public void teleopPeriodic() {

    robotDrive.setSafetyEnabled(false);
    double speedCap = .75;
    double spinCap = .5;
    //robotDrive.driveCartesian(-speedCap*xbox2.getRawAxis(1), speedCap*xbox2.getRawAxis(0), speedCap*xbox2.getRawAxis(4));

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

    // xbox controller 1 A button shoots
    if (xbox1.getAButton())
    {
       shooterLeft.set(0.35);    
       //shooterRight.set(-1.0);
    }
    // Xbox controller 1 B Button reverses shooter (in case ball gets stuck in intake)
    else if (xbox1.getBButton())
    {
      shooterLeft.set(-0.35);  
      //shooterRight.set(1);
    }
    // Else the shooter motors stop
    else
    {
      shooterLeft.stopMotor();
      shooterRight.stopMotor();
    }

    //Turns the intake on if X is pressed
    if(xbox1.getXButton())
    {
      intakeMotor.set(-0.80);
    }
    //Turns the intake backwards if Y is pressed
    else if(xbox1.getYButton()) 
    {
      intakeMotor.set(0.80); 
    }

    //Stops the motor if nothin is being pressed
    else
    {
      intakeMotor.stopMotor();
    }
    
    /*pneumatics button (same controller as intake and shooter) if left bumper is pressed the pneumatics should activate
    if (xbox1.getLeftBumper())
    {
      pneumaticClimber.set(true);
       //m_doubleSolenoid.set(DoubleSolenoid.Value.kForward);
    }
    else 
    {
     pneumaticClimber.set(false);
     //m_doubleSolenoid.set(DoubleSolenoid.Value.kReverse);
   }
 */
    //SmartDashboard.putNumber("intakeDeployEncoder",intakeDeploy.getSelectedSensorPosition());

   /* 
        if(xbox1.getXButton()){
          intakeDeployPos = INTAKE_DOWN;
        }
        else if (xbox1.getYButton()){
          intakeDeployPos = INTAKE_UP;
        }
        intakeDeploy.set((Math.abs(intakeDeployPos - intakeDeploy.getSelectedSensorPosition()) > 300)? (intakeDeployPos - intakeDeploy.getSelectedSensorPosition()):0);
    */
    /*
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
    }*/

  }

  @Override
  public void autonomousInit() {
    //m_autonomousCommand = m_chooser.getSelected();

    robotDrive.setSafetyEnabled(false);
    timer = new Timer();
    timer.reset();
    timer.start();

    //Declaring variabled and arrays
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

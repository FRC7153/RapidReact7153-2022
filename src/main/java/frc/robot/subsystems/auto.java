package frc.robot.subsystems;

import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import com.ctre.phoenix.motorcontrol.can.TalonFX;

import java.util.Collection;
import java.util.List;
import com.ctre.phoenix.motorcontrol.ControlMode;
import edu.wpi.first.wpilibj.drive.MecanumDrive;
import edu.wpi.first.wpilibj.Timer;
import frc.robot.subsystems.mecanumOdometry;

// Vision Imports
import edu.wpi.first.cscore.UsbCamera;
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.vision.VisionThread;
import org.opencv.core.*;
import frc.robot.visiontargets.BlueBallPipeline;

public class auto {
    // Drive elements
    private shooterPID shooter;
    private TalonFX greenWheel;
    private MecanumDrive mecDrive;
    private mecanumOdometry mecOdometry;
    private autoCenter center;
    private solenoidCode solenoid; 
    private talonSRXwheel intake;
 

    // Vision Pipelines
    private final Object imgLock = new Object();
    private VisionThread visionThread; 
   

    private int videoWidth = 320;
    private int videoHeight = 240;

    private UsbCamera camera1;

    // Auto
    double start;
    int step;

    public auto(
            shooterPID shooterDrive, 
            autoCenter C, 
            mecanumOdometry odo, 
            TalonFX greenIntakeWheel, 
            MecanumDrive mecanumDrive, 
            UsbCamera cam1, 
            String robotColor, 
            solenoidCode S, 
            talonSRXwheel intakeWheel
            ) {
        shooter = shooterDrive;
        greenWheel = greenIntakeWheel;
        mecDrive = mecanumDrive;
        mecOdometry = odo;
        center = C;
        this.solenoid = S;
        this.intake = intakeWheel;

        // camera1 = cam1;
        // camera1.setResolution(videoWidth, videoHeight);
        
        // visionThread = new VisionThread(
        //     camera1,
        //     (robotColor == "blue") ? new BlueBallPipeline() : new BlueBallPipeline(), // change
        //     pipeline -> {
        //         if (!pipeline.findBlobsOutput().empty()) {
        //             KeyPoint point = (pipeline.findBlobsOutput().toList().get(0));
        //             Point pt = point.pt;

        //             synchronized (imgLock) {
        //                 currentXTarget = pt.x - (videoWidth/2);
        //             }
        //         }
        //     }
        // );
        // visionThread.start();
    }

    public void autoStart() {
        step = 0;
        start = Timer.getFPGATimestamp();
    }

    private void nextStep() {
        System.out.println ("nextStep: " + step);
        step++;
        start = Timer.getFPGATimestamp();
        greenWheel.set(ControlMode.PercentOutput, 0.0);
        shooter.setSpeed(0.0);
        mecDrive.driveCartesian(0.0, 0.0, 0.0);
    }

    public void autoPeriodic() {
        if (step == 0) {
           
            shooter.setSpeed(2200.0);
            greenWheel.set(ControlMode.PercentOutput, 0.5);
            if (Timer.getFPGATimestamp() - start >= 1) { nextStep(); }
        } else if (step == 1) {
            mecDrive.driveCartesian(0.4, 0.0, 0.0);
            if (Timer.getFPGATimestamp() - start >= 1.5) { nextStep(); }
        } else if (step == 2) {
            solenoid.goToState(false);
            intake.setIntakeState(true);
             { nextStep(); }
        } else if (step == 3) {
            mecDrive.driveCartesian(0.0, 0.0, 0.35);
            if (Timer.getFPGATimestamp() - start >= 1.38) { nextStep(); }
        } else if (step == 4) {
            mecDrive.driveCartesian(-0.4, 0.0, 0.0);
            if (Timer.getFPGATimestamp() - start >= 1.3) { nextStep(); } //was 1.7
        } else if (step == 5) {
            solenoid.goToState(true);
            intake.setIntakeState(false); 
            if (Timer.getFPGATimestamp() - start >= 0.7) 
            { nextStep(); }
        } else if (step == 6) {
            mecDrive.driveCartesian(-0.0, 0.0, -.35);
            if (Timer.getFPGATimestamp() - start >= 1.38) { nextStep(); }
        } else if (step == 7) {
            mecDrive.driveCartesian(-0.4, 0.0, 0.0);
            if (Timer.getFPGATimestamp() - start >= 1.1) { nextStep(); }  //was 2
        } else if (step == 8) {
            mecDrive.driveCartesian(0.0, 0.0, center.getTurn());
            if (center.isInTarget()) { nextStep(); }
        } else if (step == 9) {
            shooter.setSpeed(center.getSuggestedSpeed());
            if (Timer.getFPGATimestamp() - start >= 1.0)
            { 
                step++;
                 start = Timer.getFPGATimestamp();
             }
        } else if (step == 10) {
            
            greenWheel.set(ControlMode.PercentOutput, 0.5);
            if (Timer.getFPGATimestamp() - start >= 3.0) { nextStep(); }
        }
    }

    public void autoOdometry() {
        if (step == 0) {
            mecOdometry.updateSetpoints(3.0, 0.0, 0.0);
            mecOdometry.robotDisabled = false;
            step++;
        } else if (step == 1) {
            if (mecOdometry.setpointsDone) {
                mecOdometry.robotDisabled = true;
                nextStep();
            }
        }
    }

 


}
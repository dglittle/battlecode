package team000;

import battlecode.common.*;
import static battlecode.common.GameConstants.*;

public class RobotPlayer implements Runnable {

   private final RobotController myRC;

   public RobotPlayer(RobotController rc) {
      myRC = rc;
   }

   public void run() {
      while(true){
         try{
            /*** beginning of main loop ***/

            while(myRC.isMovementActive()) {
               myRC.yield();
            }

            if(myRC.canMove(myRC.getDirection())) {
               myRC.moveForward();
            }
            else {
               myRC.setDirection(myRC.getDirection().rotateRight());
            }
            myRC.yield();

            /*** end of main loop ***/
         }catch(Exception e) {
            System.out.println("caught exception:");
            e.printStackTrace();
         }
      }
   }
}

package flyingRush.test;

import battlecode.common.ComponentType;
import battlecode.common.Robot;
import battlecode.common.RobotInfo;
import flyingRush.core.S;
import flyingRush.core.xconst.XChassis;

public final class SensorTest {
  public static void debug_Test() {
    testInit();
    testAfterMoving();
    testAfterTurning();
    testAfterRound();
    
    // NOTE: the light chassis doesn't have an omnidirectional sensor, might not see the mines
    if (S.chassisInt == XChassis.BUILDING_INT) {
      testNearbyRobots();
      testNearbyRobotInfo();
    }
  }
  
  public static void testInit() {
    S.birthRound = -1;
    S.chassisInt = -1;
    S.leftoverWeight = -1;
    S.chassisWeight = -1;
    S.id = -1;
    
    S.init(S.rc);

    if (S.birthRound == -1) { throw new RuntimeException("birthRound not initialized"); }    
    if (S.rc == null) { throw new RuntimeException("S.rc not initialized"); }
    if (S.r == null) { throw new RuntimeException("S.r not initialized"); }
    if (S.chassis == null) { throw new RuntimeException("S.chassis not initialized"); }
    if (S.chassisInt == -1) { throw new RuntimeException("S.chassisInt not initialized"); }
    if (S.leftoverWeight == -1) { throw new RuntimeException("S.leftoverWeight not initialized"); }
    if (S.chassisWeight == -1) { throw new RuntimeException("S.chassisInt not initialized"); }
    if (S.team == null) { throw new RuntimeException("S.team not initialized"); }
    if (S.id == -1) { throw new RuntimeException("S.id not initialized"); }
  }
  
  
  public static void testAfterMoving() {
    S.location = null;
    S.locationX = -1;
    S.locationY = -1;
    
    S._updateSensorsAfterMoving();
    
    if (S.location == null) { throw new RuntimeException("S.location not initialized"); }
    if (S.locationX == -1) { throw new RuntimeException("S.locationX not initialized"); }
    if (S.locationY == -1) { throw new RuntimeException("S.locationY not initialized"); }
  }
  
  public static void testAfterTurning() {
    S.direction = null;
    S.directionInt = -1;
    
    S._updateSensorsAfterTurning();
    
    if (S.direction == null) { throw new RuntimeException("S.direction not initialized"); }
    if (S.directionInt == -1) { throw new RuntimeException("S.directionInt not initialized"); }    
  }
  
  public static void testAfterRound() {
    S.flux = -1;
    S.oldFlux = -2;
    S.hp = -1;
    S.oldHp = -2;
    
    S._updateSensorsAfterRound();
  
    if (S.flux == -1) { throw new RuntimeException("S.flux not initialized"); }
    if (S.oldFlux != -1) { throw new RuntimeException("S.oldFlux incorrect"); }
    if (S.dFlux < 0) { throw new RuntimeException("S.dFlux incorrect"); }
    if (S.hp == -1) { throw new RuntimeException("S.hp not initialized"); }
    if (S.oldHp != -1) { throw new RuntimeException("S.oldHp incorrect"); }
    if (S.dFlux < 0) { throw new RuntimeException("S.dHp incorrect"); }
    if (S.movementController == null) { throw new RuntimeException("S.movementController not initialized"); }
    if (S.sensorController == null)  { throw new RuntimeException("S.sensorController not initialized"); }
    
    if (S.sensorType == ComponentType.BUILDING_SENSOR) {
      if (S.sensorIsOmnidirectional == false) {
        throw new RuntimeException("S.sensorIsOmnidirectional not initialized");
      }
    }
  }
  
  public static void testNearbyRobots() {
    S._nearbyRobotsRound = -1;
    
    Robot[] robots = S.nearbyRobots();
    if (robots.length == 0) { throw new RuntimeException("S.nearbyRobots() returns empty array"); }
    if (S._nearbyRobotsRound == -1) { throw new RuntimeException("S.nearbyRobots() does not update cache"); }
  }
  
  public static void testNearbyRobotInfo() {
    S._nearbyRobotsRound = -1;
    
    RobotInfo[] robotInfo = S.nearbyRobotInfos();
    if (robotInfo.length == 0) { throw new RuntimeException("S.nearbyRobotInfo() returns empty array"); }
    if (S._nearbyRobotsRound == -1) { throw new RuntimeException("S.nearbyRobotInfo() does not update cache"); }    
  }
}

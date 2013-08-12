package team050.chaos.little;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import battlecode.common.Chassis;
import battlecode.common.ComponentType;
import battlecode.common.GameConstants;

class Comp {
  public ComponentType comp;
  public int maxCount = Integer.MAX_VALUE;
  public int busyRounds = 0;

  public Comp(ComponentType comp) {
    this.comp = comp;
  }

  public void init(Robot a) {
  }

  public void roundEnd(Robot a) {
    busyRounds--;
  }

  public double shield(double amount) {
    return amount;
  }

  public double attack(Robot a, Robot b) {
    return 0;
  }
}

class Jump extends Comp {
  public Jump() {
    super(ComponentType.JUMP);
  }

  public boolean canJump() {
    return busyRounds <= 0;
  }

  public boolean jump() {
    if (canJump()) {
      busyRounds = comp.delay;
      return true;
    }
    return false;
  }
}

class Vision extends Comp {
  public Vision(ComponentType ct) {
    super(ct);
    maxCount = 1;
  }

  public void init(Robot a) {
    a.sightRange = (int) Math.max(a.sightRange,
        Math.round(Math.sqrt(comp.range)));
  }
}

class Sight extends Vision {
  public Sight() {
    super(ComponentType.SIGHT);
  }
}

class Radar extends Vision {
  public Radar() {
    super(ComponentType.RADAR);
  }
}

class Shield extends Comp {
  public Shield() {
    super(ComponentType.SHIELD);
  }

  public double shield(double amount) {
    if (amount >= GameConstants.SHIELD_MIN_DAMAGE) {
      return Math.max(amount - GameConstants.SHIELD_DAMAGE_REDUCTION,
          GameConstants.SHIELD_MIN_DAMAGE);
    }
    return amount;
  }
}

class Hardened extends Comp {
  public Hardened() {
    super(ComponentType.HARDENED);
    maxCount = 1;
  }

  public double shield(double amount) {
    return Math.min(amount, GameConstants.HARDENED_MAX_DAMAGE);
  }
}

class Regen extends Comp {
  public Regen() {
    super(ComponentType.REGEN);
  }

  public void roundEnd(Robot a) {
    super.roundEnd(a);
    a.hp = Math.min(a.hp + GameConstants.REGEN_AMOUNT, a.maxHp);
  }
}

class Plasma extends Comp {
  public Plasma() {
    super(ComponentType.PLASMA);
  }

  public double shield(double amount) {
    if (busyRounds <= 0) {
      if (amount > 0) {
        busyRounds = comp.delay;
        return 0;
      }
    }
    return amount;
  }
}

class Iron extends Comp {
  public int onRounds = 0;

  public Iron() {
    super(ComponentType.IRON);
    maxCount = 1;
    turnOn();
  }

  public void turnOn() {
    onRounds = 2;
    busyRounds = ComponentType.IRON.delay;
  }

  public double shield(double amount) {
    return (4.0 / 5.0) * amount;
    // if (onRounds > 0) {
    // return 0;
    // }
    // return amount;
  }

  public void roundEnd(Robot a) {
    super.roundEnd(a);
    onRounds--;
    if (busyRounds <= 0) {
      turnOn();
    }
  }
}

class Plating extends Comp {
  public Plating() {
    super(ComponentType.PLATING);
  }

  public void init(Robot a) {
    a.maxHp += GameConstants.PLATING_HP_BONUS;
  }
}

class Gun extends Comp {
  public int range = 0;

  public Gun(ComponentType ct) {
    super(ct);
    range = (int) Math.round(Math.sqrt(ct.range));

  }

  public void init(Robot a) {
    a.desiredDist = (int) Math.min(a.desiredDist, range);
  }

  public double attack(Robot a, Robot b) {
    int dist = Math.abs(a.x - b.x);
    if (busyRounds <= 0 && dist <= range && dist <= a.sightRange) {
      busyRounds = comp.delay;
      return comp.attackPower;
    }
    return 0;
  }
}

class SMG extends Gun {
  public SMG() {
    super(ComponentType.SMG);
  }
}

class Blaster extends Gun {
  public Blaster() {
    super(ComponentType.BLASTER);
  }
}

class Railgun extends Gun {
  public Railgun() {
    super(ComponentType.RAILGUN);
  }
}

class Hammer extends Gun {
  public Hammer() {
    super(ComponentType.HAMMER);
  }
}

class Beam extends Gun {
  public int lastX = 0;
  public int counter = 0;

  public Beam() {
    super(ComponentType.BEAM);
  }

  public double attack(Robot a, Robot b) {
    int dist = Math.abs(a.x - b.x);
    if (busyRounds <= 0 && dist <= range && dist <= a.sightRange) {
      busyRounds = comp.delay;
      if (b.x == lastX) {
        counter++;
        if (counter >= GameConstants.BEAM_RAMP.length) {
          counter = GameConstants.BEAM_RAMP.length - 1;
        }
      } else {
        counter = 0;
      }
      lastX = b.x;
      return GameConstants.BEAM_RAMP[counter];
    }
    return 0;
  }
}

class Robot {
  public Chassis chassis = null;
  public ArrayList<Comp> comps = new ArrayList<Comp>();
  int x;
  public double hp;
  public double maxHp;

  public int sightRange = 0;
  public int desiredDist = Integer.MAX_VALUE;

  public ArrayList<Jump> jumps = new ArrayList<Jump>();

  public int moveBusyRounds = 0;

  public Robot(Chassis chassis, ArrayList<Comp> comps, int x) {
    this.chassis = chassis;
    this.comps = comps;
    this.x = x;
    init();
  }

  public void init() {
    maxHp = chassis.maxHp;
    for (int i = comps.size() - 1; i >= 0; i--) {
      Comp c = comps.get(i);
      c.init(this);
      if (c instanceof Jump) {
        comps.remove(i);
        jumps.add((Jump) c);
      }
    }
    hp = maxHp;
  }

  public void roundEnd(Robot b) {
    for (Comp c : comps) {
      c.roundEnd(this);
    }
    final int wall = 26;

    int dist = Math.abs(x - b.x);
    if (dist <= sightRange) {
      int dx = jumpOrMove(dist, (x < b.x) ? (b.x - -wall) : (wall - b.x));
      if (dx != 0) {
        if (x < b.x)
          x -= dx;
        else
          x += dx;
      }
    }

    moveBusyRounds--;
  }

  public int jumpOrMove(int dist, int wall) {
    int bestJumpPosition = dist;
    int bestMovePosition = dist;
    if (dist < desiredDist) {
      bestJumpPosition = Math.min(desiredDist, wall);
      if (bestJumpPosition - dist > 4)
        bestJumpPosition = dist + 4;
      bestMovePosition = Math.min(dist + 1, wall);
    } else {
      bestJumpPosition = desiredDist - 1;
      if (bestJumpPosition - dist < -4)
        bestJumpPosition = dist - 4;
      bestMovePosition = dist - 1;
    }
    if (bestJumpPosition != bestMovePosition) {
      for (Jump j : jumps) {
        if (j.jump()) {
          return bestJumpPosition - dist;
        }
      }
    }
    if (moveBusyRounds <= 0) {
      moveBusyRounds = chassis.moveDelayOrthogonal;
      return bestMovePosition - dist;
    }
    return 0;
  }

  public void attack(Robot b) {
    for (Comp c : comps) {
      double power = c.attack(this, b);
      for (Comp bc : b.comps) {
        power = bc.shield(power);
      }
      b.hp -= power;
    }
  }

  public String toString() {
    StringBuffer buf = new StringBuffer();
    buf.append(chassis + " " + weight(comps) + "\n");
    for (Comp c : comps) {
      buf.append(c.comp + "\n");
    }
    for (Jump j : jumps) {
      buf.append(j.comp + "\n");
    }
    return buf.toString();
  }

  public boolean hasRegen() {
    for (Comp c : comps) {
      if (c.comp == ComponentType.REGEN)
        return true;
    }
    return false;
  }

  public int numRegens() {
    int count = 0;
    for (Comp c : comps) {
      if (c.comp == ComponentType.REGEN) {
        count++;
      }
    }
    return count;
  }

  public int weight(ArrayList<Comp> comps) {
    int sum = 0;
    for (Comp c : comps) {
      sum += c.comp.weight;
    }
    for (Jump j : jumps) {
      sum += j.comp.weight;
    }
    return sum;
  }
}

class MyArray {
  public int[] a;

  public MyArray(int[] a) {
    this.a = a.clone();
  }

  @Override
  public boolean equals(Object b) {
    return Arrays.equals(a, ((MyArray) b).a);
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(a);
  }
}

public class Battle {
  public static Comp[] comps = new Comp[]{
      new Jump(), new Sight(), new Radar(), new Shield(), new Hardened(),
      new Regen(), new Plasma(), new Iron(), new Plating(), new SMG(),
      new Hammer(), new Blaster(), new Beam(), new Railgun()};

  // public static Comp[] comps = new Comp[]{
  // new Sight(), new Radar(), new Shield(), new Hardened(), new Regen(),
  // new Plasma(), new Iron(), new Plating(), new SMG(),
  // new Blaster(), new Beam(), new Railgun()};

  public static Comp getComp(ComponentType ct) {
    for (Comp c : comps) {
      if (c.comp == ct)
        return c;
    }
    return null;
  }

  public Robot createRobot(Chassis chassis, int[] counts, int x)
      throws Exception {
    ArrayList<Comp> comps = new ArrayList<Comp>();
    for (int ci = 0; ci < counts.length; ci++) {
      int count = counts[ci];
      for (int i = 0; i < count; i++) {
        comps.add((Comp) Battle.comps[ci].getClass().newInstance());
      }
    }
    return new Robot(chassis, comps, x);
  }

  public Robot createRobot(Chassis chassis, Comp[] compsTemplate, int x)
      throws Exception {
    ArrayList<Comp> comps = new ArrayList<Comp>();
    for (int ci = 0; ci < compsTemplate.length; ci++) {
      comps.add((Comp) compsTemplate[ci].getClass().newInstance());
    }
    return new Robot(chassis, comps, x);
  }

  public boolean inc(Chassis chassis, int[] counts) {
    for (int i = 0; i < counts.length; i++) {
      counts[i]++;
      if (counts[i] <= comps[i].maxCount) {
        int weight = weight(counts);
        if (weight <= chassis.weight)
          return true;
      }
      counts[i] = 0;
    }
    return false;
  }

  public static void main(String[] args) throws Exception {
    new Battle().go();
  }

  public void go() throws Exception {
    Chassis chassis = Chassis.HEAVY;

    Set<MyArray> goodOnes = new HashSet<MyArray>();

    if (true) {
      
      
      
      ArrayList<Comp> comps = new ArrayList<Comp>();
      comps.add(new Jump());
      comps.add(new Railgun());
      comps.add(new Railgun());
      //comps.add(new Railgun());
      comps.add(new SMG());
      comps.add(new Shield());
      comps.add(new Radar());
      Robot a = new Robot(chassis, comps, -2);
      
      VictorAlg va = new VictorAlg(chassis, comps);
      Robot b = va.createRobot(2);
//
//      comps = new ArrayList<Comp>();
//      comps.add(new Radar());
//      comps.add(new Railgun());
//      comps.add(new Railgun());
//      comps.add(new Railgun());
//      Robot b = new Robot(chassis, comps, 2);

      double beat = beats(a, b, true);
      System.out.println("beat = " + beat);
      
      System.out.println("b: " + b);

      System.exit(0);
    }

    if (true) {
      int[] theirCounts = new int[comps.length];
      int safe = 0;
      while (inc(chassis, theirCounts)) {
        safe++;

        Robot theirRobot = createRobot(chassis, theirCounts, 2);
        Robot ourRobot = new VictorAlg(chassis, theirRobot.comps).createRobot(-2);

        double beat = beats(ourRobot, theirRobot, false);
        // double beat = -beats(theirRobot, ourRobot, false);

        if (Math.random() < 0.01) {
          System.out.println(safe + ": beat: " + beat);
        }

        if (beat < 0 || ourRobot.weight(ourRobot.comps) > 18) {

          // make sure it has a jump
          if (theirCounts[0] > 0) {

            theirRobot = createRobot(chassis, theirCounts, 2);
            ourRobot = new VictorAlg(chassis, theirRobot.comps).createRobot(-2);

            beats(ourRobot, theirRobot, true);
            System.out.println("theirRobot: " + theirRobot);
            System.out.println("ourRobot: " + ourRobot);
            System.out.println("beat: " + beat);

            System.out.println("i: " + safe);

            break;
          }
        }
      }

      System.out.println("done!");

      System.exit(0);
    }

    goodOnes.add(new MyArray(new int[comps.length]));

    while (true) {
      double best = -Double.MAX_VALUE;
      int[] cCounts = null;

      int[] bCounts = new int[comps.length];
      while (inc(chassis, bCounts)) {
        double beatAccum = 0;
        int beatCount = 0;
        boolean beatsAll = true;
        for (MyArray ma : goodOnes) {
          double beat = -beats(chassis, ma.a, chassis, bCounts, false);
          if (beat <= 0) {
            beatsAll = false;
            break;
          }
          beatAccum += beat;
          beatCount++;
        }
        if (beatsAll) {
          double beatAvg = beatAccum / beatCount;
          if (beatAvg > best) {

            System.out.println("beat: " + (1.0 / (beatAvg - 10)));
            System.out.println("robot b: " + createRobot(chassis, bCounts, 0));

            best = beatAvg;
            cCounts = bCounts.clone();
          }
        }
      }

      if (cCounts == null) {
        System.out.println("optimal!");
        break;
      }

      if (goodOnes.add(new MyArray(cCounts))) {

        System.out.println("-------------------");
        System.out.println("robot b: " + createRobot(chassis, cCounts, 0));

      } else {
        break;
      }
    }
    System.out.println("done! ========================");
    int i = 1;
    for (MyArray a : goodOnes) {
      System.out.println("GOOD: " + i + ":" + createRobot(chassis, a.a, 0));
      i++;
    }
    int y = 1;
    for (MyArray a : goodOnes) {
      System.out.print("type" + y);
      int x = 1;
      for (MyArray b : goodOnes) {
        double beat = beats(chassis, a.a, chassis, b.a, false);
        System.out.print("\t" + beat);
        x++;
      }
      System.out.println();
      y++;
    }

    // for (MyArray a : goodOnes) {
    // System.out.println("robot: " + createRobot(aChassis, a.a, 0));
    // }

    // boolean aFlag = true;
    // boolean bFlag = true;
    // while (aFlag && bFlag) {
    // while (aFlag = inc(aChassis, aCounts)) {
    // if (beats(aChassis, aCounts, bChassis, bCounts, false) > 0) {
    // break;
    // }
    // }
    //
    // // work here
    // // beats(aChassis, aCounts, bChassis, bCounts, true);
    // System.out.println("a: " + createRobot(aChassis, aCounts, 0));
    //
    // while (bFlag = inc(bChassis, bCounts)) {
    // if (beats(aChassis, aCounts, bChassis, bCounts, false) < 0) {
    // break;
    // }
    // }
    //
    // // work here
    // // beats(aChassis, aCounts, bChassis, bCounts, true);
    // System.out.println("b: " + createRobot(bChassis, bCounts, 0));
    //
    // // work here
    // // if (true)
    // // break;
    // }
  }

  public double beatsBothSides(Chassis aChassis, int[] aCounts,
      Chassis bChassis, int[] bCounts) throws Exception {
    double a = beats(aChassis, aCounts, bChassis, bCounts, false);
    double b = -beats(bChassis, bCounts, aChassis, aCounts, false);
    return a + b;
  }

  public double beats(Chassis aChassis, int[] aCounts, Chassis bChassis,
      int[] bCounts, boolean show) throws Exception {
    Robot a = createRobot(aChassis, aCounts, -2);
    Robot b = createRobot(bChassis, bCounts, 2);
    return beats(a, b, show);
  }

  public double beats(Robot a, Robot b, boolean show) throws Exception {
    int round = 0;
    while (a.hp > 0 && b.hp > 0 && round < 1000) {
      round++;
      a.attack(b);
      b.attack(a);
      if (round % 2 == 0)
        a.roundEnd(b);
      else
        b.roundEnd(a);

      if (round >= 10) {
        if (a.hp == a.maxHp && b.hp == b.maxHp) {
          return 0;
        }
      }

      // work here
      if (show) {
        System.out.println("round: " + round);
        System.out.println("a:" + a.x + " " + a.hp + ", b:" + b.x + " " + b.hp
            + " des:" + a.desiredDist);
      }
    }
    // if (a.hp > (a.maxHp / 2) && b.hp <= 0) {
    if (a.hp > 0 && b.hp <= 0) {
      if (a.hasRegen()) {
        round += Math.ceil((a.maxHp - a.hp)
            / (GameConstants.REGEN_AMOUNT * a.numRegens()));
        a.hp = a.maxHp;
      }
      return 1.0 / round + (a.hp == a.maxHp ? 10 : 0);
      // } else if (a.hp <= 0 && b.hp > (b.maxHp / 2)) {
    } else if (a.hp <= 0 && b.hp > 0) {
      if (b.hasRegen()) {
        round += Math.ceil((b.maxHp - b.hp)
            / (GameConstants.REGEN_AMOUNT * b.numRegens()));
        b.hp = b.maxHp;
      }
      return -1.0 / round - (b.hp == b.maxHp ? 10 : 0);
    }
    return 0;
  }

  public int weight(int[] counts) {
    int sum = 0;
    for (int i = 0; i < counts.length; i++) {
      sum += comps[i].comp.weight * counts[i];
    }
    return sum;
  }
}

class VictorAlg {
  public Chassis chassis = null;
  public int[] ourCounts = new int[ComponentType.values().length];
  public int[] theirCounts = new int[ComponentType.values().length];

  public int our(ComponentType comp) {
    return ourCounts[comp.ordinal()];
  }

  public boolean addOur(ComponentType comp, int amount) {
    if (weight() + comp.weight * amount <= chassis.weight) {
      ourCounts[comp.ordinal()] += amount;
      return true;
    }
    return false;
  }

  public int setOur(ComponentType comp, int amount) {
    ourCounts[comp.ordinal()] = amount;
    return ourCounts[comp.ordinal()];
  }

  public int their(ComponentType comp) {
    return theirCounts[comp.ordinal()];
  }

  public int weight() {
    int w = 0;
    for (int i = 0; i < ourCounts.length; i++) {
      w += ourCounts[i] * ComponentType.values()[i].weight;
    }
    return w;
  }

  public Robot createRobot(int x) throws Exception {
    ArrayList<Comp> comps = new ArrayList<Comp>();
    for (int ci = 0; ci < ourCounts.length; ci++) {
      int count = ourCounts[ci];
      for (int i = 0; i < count; i++) {
        comps.add((Comp) Battle.getComp(ComponentType.values()[ci]).getClass().newInstance());
      }
    }
    return new Robot(chassis, comps, x);
  }

  public VictorAlg(Chassis chassis, ArrayList<Comp> theirComps) {
    this.chassis = chassis;
    for (Comp c : theirComps) {
      theirCounts[c.comp.ordinal()]++;
    }

    int weapons = 0;
    double mostPowerful = 0;
    for (Comp c : theirComps) {
      if (c instanceof Gun) {
        weapons++;
        double a = c.comp.attackPower;
        if (c instanceof Beam) {
          a = 6.0;
        }
        if (a > mostPowerful) {
          mostPowerful = a;
        }
      }
    }

    // //////////////

    // work here

    // RADAR
    // SHIELD
    // PLASMA
    // SMG
    // SMG
    // SMG
    // BLASTER
    // BEAM
    // JUMP
    // addOur(ComponentType.RADAR, 1);
    // addOur(ComponentType.SHIELD, 1);
    // addOur(ComponentType.PLASMA, 1);
    // addOur(ComponentType.SMG, 3);
    // addOur(ComponentType.BLASTER, 1);
    // addOur(ComponentType.BEAM, 1);
    // addOur(ComponentType.JUMP, 1);
    // if (true)
    // return;

    addOur(ComponentType.RADAR, 1);
    addOur(ComponentType.JUMP, 1);

    int neededPlasmas = weapons + their(ComponentType.HAMMER);

    // try wall 1
    if (addOur(ComponentType.SMG, their(ComponentType.PLASMA))
        && addOur(ComponentType.PLASMA, neededPlasmas)
        && (addOur(ComponentType.RAILGUN, 1) || ((their(ComponentType.REGEN) == 0) && addOur(
            ComponentType.SMG, 1)))) {

    } else {
      ourCounts = new int[ComponentType.values().length];

      // try wall 2
      if (addOur(ComponentType.RADAR, 1)
          && addOur(ComponentType.JUMP, 1)
          && addOur(ComponentType.SMG, their(ComponentType.PLASMA))
          && addOur(ComponentType.SHIELD,
              (int) Math.ceil((mostPowerful - 0.15) / 0.6))
          && addOur(ComponentType.REGEN, 1) && addOur(ComponentType.SMG, 1)) {

      } else {
        ourCounts = new int[ComponentType.values().length];

        addOur(ComponentType.RADAR, 1);
        addOur(ComponentType.JUMP, 1);

        addOur(ComponentType.SMG, their(ComponentType.PLASMA));

        if (their(ComponentType.SMG) > 0) {
          addOur(ComponentType.SHIELD, 1);

          if (their(ComponentType.SMG) >= 4) {
            addOur(ComponentType.REGEN, 1);
          }
        }

        while (addOur(ComponentType.RAILGUN, 1)) {
        }
      }
    }

    // fill up
    while (weight() < chassis.weight) {
      addOur(ComponentType.SMG, 1);
      // addOur(ComponentType.SHIELD, 1);
    }
    // if (our(ComponentType.REGEN) == 0) {
    // addOur(ComponentType.REGEN, 1);
    // }
    // if (their(ComponentType.HAMMER) == 0) {
    // while (addOur(ComponentType.BLASTER, 1)) {
    // }
    // }

    // //////////////////////////////////

    if (true) {
      return;
    }

    // sight
    addOur(ComponentType.RADAR, 1);

    // defense
    boolean nohammer = false;
    if (their(ComponentType.SMG) > 0) {
      addOur(ComponentType.SHIELD, 1);
      addOur(ComponentType.REGEN,
          (int) Math.ceil((double) their(ComponentType.SMG) / 5));
    }
    if (their(ComponentType.BLASTER) > 0) {
      double option1Weight = ComponentType.PLASMA.weight
          * their(ComponentType.BLASTER);
      double option2Weight = ComponentType.SHIELD.weight
          * (4 - our(ComponentType.SHIELD)) + ComponentType.REGEN.weight
          * Math.ceil((double) their(ComponentType.BLASTER) / 5);
      if (option1Weight < option2Weight) {
        addOur(ComponentType.PLASMA, their(ComponentType.BLASTER));
      } else {
        setOur(ComponentType.SHIELD, Math.max(our(ComponentType.SHIELD), 4));
        addOur(ComponentType.REGEN,
            (int) Math.ceil((double) their(ComponentType.BLASTER) / 5));
      }
    }
    addOur(ComponentType.PLASMA, their(ComponentType.RAILGUN));
    if (their(ComponentType.HAMMER) > 0) {
      nohammer = true;
    }
    addOur(ComponentType.PLASMA, their(ComponentType.BEAM));

    // offense
    addOur(ComponentType.SMG, their(ComponentType.PLASMA));
    boolean small = (their(ComponentType.HARDENED) > 0)
        || (their(ComponentType.IRON) > 0);
    boolean big = their(ComponentType.REGEN) > 0;
    if (big) {
      addOur(ComponentType.RAILGUN, 1);
    }
    if (small) {
      addOur(ComponentType.BLASTER, 1);
    }

    // filler...
    addOur(ComponentType.REGEN, 1);
    if (!small) {
      while (weight() < chassis.weight - ComponentType.RAILGUN.weight) {
        addOur(ComponentType.RAILGUN, 1);
      }
    } else {
      while (weight() < chassis.weight - ComponentType.BLASTER.weight) {
        addOur(ComponentType.BLASTER, 1);
      }
    }

    while (addOur(ComponentType.PLASMA, 1)) {
    }
    while (addOur(ComponentType.PLATING, 1)) {
    }
  }
}

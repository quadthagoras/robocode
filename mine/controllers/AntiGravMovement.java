package mine.controllers;

import javafx.geometry.Point2D;
import javafx.geometry.Rectangle2D;

import mine.models.EnemyRobot;
import robocode.AdvancedRobot;
import robocode.util.Utils;

public class AntiGravMovement {
	
	protected AdvancedRobot me;
	protected EnemyRobot enemy;
	
	protected Rectangle2D battleField;

	private final double enemyForceFactor = 10.0;
	private final double perpendicularForceFactor = 2.0;
	private final double wallForceFactor = 600.0;
	private final double cornerForceFactor = 600.0;
	protected int direction = 1;
	
	private Point2D[] corners = new Point2D [4];

	private double turn;
	private double ahead;
	
	private long lastHit = 0;

	public AntiGravMovement(AdvancedRobot me, EnemyRobot enemy) {
		this.me = me;
		this.enemy = enemy;
		this.battleField = new Rectangle2D(18, 18, me.getBattleFieldWidth() - 36, me.getBattleFieldHeight() - 36);
		
		corners[0] = new Point2D(0, 0);
		corners[1] = new Point2D(me.getBattleFieldWidth(), 0);
		corners[2] = new Point2D(0, me.getBattleFieldHeight());
		corners[3] = new Point2D(me.getBattleFieldWidth(), me.getBattleFieldHeight());
	}

	public void update() {
		getForceToCenter();
		
		Point2D force = new Point2D(0, 0);

		// enemy force
		force = addEnemyForce(force);

		// wall force
		force = addWallForce(force);

		// corner force
		force = addCornerForce(force);

		// perpendicular force
		force = addPerpendicularForce(force);

		// generate movement based on cumulative force
		generateMovement(force);
	}

	public void run() {
		me.setAhead(ahead);
		me.setTurnRightRadians(turn);
	}
	
	public void hit() {
		if(me.getTime() - lastHit > 5) {
			direction = -direction;
			lastHit = me.getTime();
		}
	}
	
	protected void getForceToCenter() {
		if (me.getX() < 50 || me.getBattleFieldWidth() - me.getX() < 50 || me.getY() < 50 || me.getBattleFieldHeight() - me.getY() < 50) {
			Point2D relCenter = new Point2D((battleField.getMinX() + battleField.getMaxX()) / 2 - me.getX(), 
					(battleField.getMinY() + battleField.getMaxY()) / 2 - me.getY());
			Point2D relEnemy = new Point2D(enemy.getX() - me.getX(), enemy.getY() - me.getY());
			if (relEnemy.crossProduct(relCenter).getZ() <= 0) {
				direction = 1;
			} else {
				direction = -1;
			}
		}
	}
	
	protected Point2D addEnemyForce(Point2D force) {
		force = force.subtract(enemyForceFactor * Math.sin(enemy.getAbsoluteBearing()) / Math.pow(enemy.getDistance() - 150, 2),
				enemyForceFactor * Math.cos(enemy.getAbsoluteBearing()) / Math.pow(enemy.getDistance() - 150, 2));
		return force;
	}
	
	protected Point2D addWallForce(Point2D force) {
		force = force.add(wallForceFactor / Math.pow(me.getX(), 3), wallForceFactor / Math.pow(me.getY(), 3));
		force = force.subtract(wallForceFactor / Math.pow(me.getBattleFieldWidth() - me.getX(), 3),
				wallForceFactor / Math.pow(me.getBattleFieldHeight() - me.getY(), 3));
		return force;
	}
	
	protected Point2D addCornerForce(Point2D force) {
		for(int i=0; i<4; i++) {
			force = force.subtract(cornerForceFactor * (corners[i].getX() - me.getX()) / Math.pow(corners[i].distance(me.getX(), me.getY()), 4),
					cornerForceFactor * (corners[i].getY() - me.getY()) / Math.pow(corners[i].distance(me.getX(), me.getY()), 4));
		}
		return force;
	}
	
	protected Point2D addPerpendicularForce(Point2D force) {
		force = force.subtract(-direction * perpendicularForceFactor * Math.cos(enemy.getAbsoluteBearing()) / Math.pow(enemy.getDistance(), 1),
				direction * perpendicularForceFactor * Math.sin(enemy.getAbsoluteBearing()) / Math.pow(enemy.getDistance(), 1));
		return force;
	}
	
	protected void generateMovement(Point2D force) {
		double angle = Math.atan2(force.getX(), force.getY());
		
		if (force.getX() != 0 || force.getY() != 0) {
			if (Math.abs(Utils.normalRelativeAngle(angle - me.getHeadingRadians())) < Math.PI / 2) {
				turn = Utils.normalRelativeAngle(angle - me.getHeadingRadians());
				ahead = Double.POSITIVE_INFINITY;
			} else {
				turn = Utils.normalRelativeAngle(angle + Math.PI - me.getHeadingRadians());
				ahead = Double.NEGATIVE_INFINITY;
			}
		}
	}

}

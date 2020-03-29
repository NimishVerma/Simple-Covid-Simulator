package social_Isolation_Simulator;
import java.util.ArrayList;
import java.util.List;


import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.parameter.Parameters;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.SimUtilities;

public class Infected {
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
//	private boolean moved;
	private int days_infected;
	private boolean in_hospital;
	Parameters params = RunEnvironment.getInstance().getParameters();
	private int max_days = (Integer)params.getValue("max_days");

	private float chance_to_infect = (Float)params.getValue("chance_to_infect");
	private float prob_dying_after_days = (Float)params.getValue("prob_dying_after_days");
	private float prob_recovering = (Float)params.getValue("prob_recovering");
	private float prob_to_go_to_hospital = (Float)params.getValue("prob_to_go_to_hospital");			
	public Infected(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;
		this.days_infected = 0;
		this.in_hospital = false;
	}

	@ScheduledMethod(start = 1, interval = 1)
	public void step() {
		boolean is_dead = check_if_dead();
		if(!is_dead) {
			// get the grid location of this Zombie
			GridPoint pt = grid.getLocation(this);

			// use the GridCellNgh class to create GridCells for
			// the surrounding neighborhood .
			GridCellNgh<Object> nghCreator = new GridCellNgh<Object>(grid, pt,
					Object.class, 1, 1);
			List<GridCell<Object>> gridCells = nghCreator.getNeighborhood(true);
			SimUtilities.shuffle(gridCells, RandomHelper.getUniform());

			GridPoint point_to_move = gridCells.get(0).getPoint();
			moveTowards(point_to_move);
			infect();
//			go_to_hospital();
			this.days_infected ++;
		}
		
		
		
	}
	public void go_to_hospital() {
		// TODO Auto-generated method stub
		
	}

	public void moveTowards(GridPoint pt) {
		// only move if we are not already in this grid location
		if (!pt.equals(grid.getLocation(this))) {
			NdPoint myPoint = space.getLocation(this);
			NdPoint otherPoint = new NdPoint(pt.getX(), pt.getY());
			double angle = SpatialMath.calcAngleFor2DMovement(space, myPoint,
					otherPoint);
			space.moveByVector(this, 1, angle, 0);
			myPoint = space.getLocation(this);
			grid.moveTo(this, (int) myPoint.getX(), (int) myPoint.getY());

		}
	}

	
	public void infect() {
		GridPoint pt = grid.getLocation(this);
		List<Object> healthy = new ArrayList<Object>();
		//Get all healthys at the new location
		
		for (Object obj : grid.getObjectsAt(pt.getX(), pt.getY())) {
			if (obj instanceof Healthy) {
				healthy.add(obj);
			}
		}
		
		//infect any random healthy
		if (healthy.size() > 0) {
			for (Object obj : healthy) {
				double random = Math.random();
				if (random <= chance_to_infect) {
					NdPoint spacePt = space.getLocation(obj);
					Context<Object> context = ContextUtils.getContext(obj);
					context.remove(obj);
					Infected infected= new Infected(space, grid);
					context.add(infected);
					space.moveTo(infected, spacePt.getX(), spacePt.getY());
					grid.moveTo(infected, pt.getX(), pt.getY());

					Network<Object> net = (Network<Object>) context
							.getProjection("infection network");
					net.addEdge(this, infected);
				}
			}
			
		}
	}
	
	
	public boolean check_if_dead() {
		GridPoint pt = grid.getLocation(this);
		if(days_infected >= max_days) {
			NdPoint spacePt = space.getLocation(this);
			Context<Object>  context = ContextUtils.getContext(this);
			context.remove(this);
			Dead dead = new Dead(space,grid);
			context.add(dead);
			space.moveTo(dead, spacePt.getX(), spacePt.getY());
			grid.moveTo(dead, pt.getX(), pt.getY());
			return true;
			
			
		}
		return false;
	}
}


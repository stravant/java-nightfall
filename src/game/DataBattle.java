package game;

import game.Agent.TurnState;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import util.Vec;

public class DataBattle {
	/////////////// board utility classes /////////////
	
	//how to draw a tile
	public static enum TileOverlay {
		None,	//no overlay
		Neg,	//red -		(neg)ative stuff
		Pos,	//blue -	(pos)itive stuff
		Mod,	//green - 	(mod)ification to the tile
		Sel,	//white - 	(sel)ection
		Move,	//box -		This is in movement range
		MoveTo,	//arrow -	(move to) here
	}
	
	//board tile
	public static class Tile {
		boolean Upload = false;
		boolean Select = false;
		boolean Flood = false;
		int FloodNum = 0;
		boolean Filled = true;
		int Credit = 0;
		Agent Agent = null;
		TileOverlay Overlay = TileOverlay.None;
	}
	
	///////////// signals for view /////////////
	
	//signal agent damaged
	public static interface ListenerAgentDamage {
		public void onAgentDamage(Vec sq);
	}
	public static class SignalAgentDamage {
		public void fire(Vec sq) {
			for (ListenerAgentDamage l : mListeners)
				l.onAgentDamage(sq);
		}
		public void connect(ListenerAgentDamage l) {
			mListeners.add(l);
		}
		private ArrayList<ListenerAgentDamage> mListeners = new ArrayList<ListenerAgentDamage>();
	}
	
	//signal agent moved
	public static interface ListenerAgentMove {
		public void onAgentMove();
	}
	public static class SignalAgentMove {
		public void fire() {
			for (ListenerAgentMove l : mListeners)
				l.onAgentMove();
		}
		public void connect(ListenerAgentMove l) {
			mListeners.add(l);
		}
		private ArrayList<ListenerAgentMove> mListeners = new ArrayList<ListenerAgentMove>();
	}
	
	//signal credits collected
	public static interface ListenerCreditCollect {
		public void onCreditCollect(Vec pos);
	}
	public static class SignalCreditCollect {
		public void fire(Vec pos) {
			for (ListenerCreditCollect l : mListeners)
				l.onCreditCollect(pos);
		}
		public void connect(ListenerCreditCollect l) {
			mListeners.add(l);
		}
		private ArrayList<ListenerCreditCollect> mListeners = new ArrayList<ListenerCreditCollect>();
	}
	
	///////////////// constructor ////////////////////
	
	public DataBattle(DataBattleInfo info, NodeMap.Node nd) {
		mTarget = nd;
		mBoardSize = info.getBoardSize();
		mBoard = new Tile[mBoardSize.getX()][mBoardSize.getY()];
		for (int x = 0; x < mBoardSize.getX(); ++x) {
			for (int y = 0; y < mBoardSize.getY(); ++y) {
				mBoard[x][y] = new Tile();
				mBoard[x][y].Filled = info.getFilled(x, y);
			}
		}
		
		//uploads
		for (DataBattleInfo.UploadEntry uent : info.getUploads()) {
			getTile(uent.Pos).Upload = true;
		}
	}
	
	////////////////// access /////////////////////
	
	public void addTeam(Team t) {
		mTeams.add(t);
	}
	
	public Collection<Team> getTeams() {
		return Collections.unmodifiableCollection(mTeams);
	}
	
	public void addAgent(Agent a) {
		mAgents.add(a);
		a.getTeam().setNumAgents(a.getTeam().getNumAgents()+1);
	}
	
	public void killAgent(Agent a) {
		mAgents.remove(a);
		a.getTeam().setNumAgents(a.getTeam().getNumAgents()-1);
	}
	
	public Collection<Agent> getAgents() {
		return Collections.unmodifiableCollection(mAgents);
	}
	
	public Tile getTile(int x, int y) {
		//bounds check
		if (x >= mBoardSize.getX() || y >= mBoardSize.getY() || x < 0 || y < 0)
			return null;
		return mBoard[x][y];
	}
	
	public Tile getTile(Vec pos) {
		return getTile(pos.getX(), pos.getY());
	}
	
	public Vec getSize() {
		return mBoardSize;
	}
	
	///////////// utility ////////////////////
	
	public void clear(boolean overlay, boolean select, boolean flood) {
		clear(overlay, select, flood, 0);
	}
	
	public void clear(boolean overlay, boolean select, boolean flood, int floodnum) {
		for (Tile[] col : mBoard)
			for (Tile tl : col) {
				if (flood) {
					tl.Flood = false;
					tl.FloodNum = floodnum;
				}
				if (overlay)
					tl.Overlay = TileOverlay.None;
				if (select)
					tl.Select = false;
			}
	}
	
	public void attackFlood(Vec pos, int range, TileOverlay overlay) {
		clear(true, true, true);
		attackFloodInternal(pos, range, overlay, 0);
	}
	private void attackFloodInternal(Vec pos, int n, TileOverlay overlay, int dist) {
		Tile tl = getTile(pos);
		if (tl == null || (tl.Flood && tl.FloodNum >= dist))
			return; //don't mess with the square

		if (tl.Filled) {
			tl.Overlay = overlay; //overlay it
		}
		
		tl.Flood = true;
		tl.FloodNum = dist;
		tl.Select = true;
		
		if (n > 0)
			//adjacent
			for (Vec v : Vec.getDirs())
				attackFloodInternal(pos.add(v), n-1, overlay, dist+1);
	}
	
	public void moveFlood(Vec pos, int range) {
		clear(true, true, true);
		Tile hometl = getTile(pos);
		TileOverlay t = hometl.Overlay;
		moveFloodInternal(hometl.Agent, pos, range, 0);
		//can't move to self, resore that tile's overlay
		hometl.Overlay = t;
		//adjacent squares should be "moveto", not "move"
		for (Vec v : Vec.getDirs()) {
			Tile atl = getTile(pos.add(v));
			if (atl != null && atl.Overlay == TileOverlay.Move) {
				atl.Overlay = TileOverlay.MoveTo;
				atl.Select = true;
			}
		}
	}
	private void moveFloodInternal(Agent a, Vec pos, int range, int dist) {
		Tile tl = getTile(pos);
		if (tl == null || (tl.Flood && tl.FloodNum >= dist) || !tl.Filled || (tl.Agent != null && tl.Agent != a))
			return;
		
		if (tl.Agent == null || tl.Agent == a)
			tl.Overlay = TileOverlay.Move;
		
		tl.Flood = true;
		
		if (range > 0)
			//adjacent
			for (Vec v : Vec.getDirs())
				moveFloodInternal(a, pos.add(v), range-1, dist+1);
	}
	
	public Team getTurn() {
		return mTeams.get(mTurn);
	}
	
	public void nextTurn() {
		mTurn++;
		if (mTurn >= mTeams.size())
			mTurn = 0;
		
		//make all agents ready again
		for (Agent a : mAgents) {
			if (a.getTeam() == mTeams.get(mTurn)) {
				a.setTurnState(TurnState.Ready);
				a.resetMoveLeft();
			}
		}
		
		//fire begin turn
		mTeams.get(mTurn).onBeginTurn.fire();
	}
	
	public NodeMap.Node getTarget() {
		return mTarget;
	}
	
	/////////////////// members //////////////////
	
	public final SignalAgentDamage onAgentDamage = new SignalAgentDamage();
	public final SignalAgentMove onAgentMove = new SignalAgentMove();
	public final SignalCreditCollect onCreditCollect = new SignalCreditCollect();
	
	private final NodeMap.Node mTarget;
	
	private final ArrayList<Team> mTeams = new ArrayList<Team>();
	private final ArrayList<Agent> mAgents = new ArrayList<Agent>();
	private int mTurn = 0;
	private Vec mBoardSize;
	private final Tile[][] mBoard;
}










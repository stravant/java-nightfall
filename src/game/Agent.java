package game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import util.Vec;

public class Agent {
	public static enum TurnState {
		Ready,	//ready to move
		Moved,	//moved
		Done,	//used ability
	}
	
	public Agent(DataBattle board, Team t, Vec startpos, AgentInfo info) {
		mInfo = info;
		mBoard = board;
		mTeam = t;
		mMaxSize = info.getSize();
		mMove = info.getMove();
		mTail.add(startpos);
		board.getTile(startpos).Agent = this;
		board.addAgent(this);
	}
	
	public void move(Vec targetsq) {
		assert targetsq != getPos(): "Attempt to move to current position";
		mBoard.onAgentMove.fire();
		mAmountMoved++;
		DataBattle.Tile tl = mBoard.getTile(targetsq);
		assert tl.Agent == null || tl.Agent == this;
		if (tl.Agent == null) { //just move
			int oldsz = mTail.size();
			//not at max size, add a new square at end
			if (oldsz < mMaxSize)
				mTail.add(mTail.get(oldsz-1));
			else
				//at max size, the tail square is now empty
				mBoard.getTile(mTail.get(oldsz-1)).Agent = null;
			
			//move tail
			for (int i = oldsz-1; i > 0; --i)
				mTail.set(i, mTail.get(i-1));
			
			//move head
			mTail.set(0, targetsq);
			tl.Agent = this; //square to move onto has this as agent
		} else if (tl.Agent == this) {
			//find the part of the tail bring moved onto
			int i;
			for (i = 1; i < mTail.size(); ++i)
				if (mTail.get(i).eq(targetsq))
					break;
			//move squares up to that point
			for (; i > 0; i--) {
				mTail.set(i, mTail.get(i-1));
			}
			//move head
			mTail.set(0, targetsq);
		}
	}
	
	public int getMoveLeft() {
		return mMove - mAmountMoved;
	}
	
	public void resetMoveLeft() {
		mAmountMoved = 0;
	}
	
	public void damage(int dmg) {
		//can't do more damage than size
		dmg = Math.min(dmg, mTail.size());
		
		//remove tail from board, notify board
		for (int i = mTail.size()-1; i > mTail.size()-1-dmg; --i) {
			mBoard.onAgentDamage.fire(mTail.get(i));
			mBoard.getTile(mTail.get(i)).Agent = null;
		}
		
		//remove own segments
		mTail.subList(mTail.size()-dmg, mTail.size()).clear();
		
		//dead
		if (mTail.size() <= 0) {
			mBoard.killAgent(this);
		}
	}
	
	public DataBattle getBoard() {
		return mBoard;
	}
	
	public Team getTeam() {
		return mTeam;
	}
	
	public int getSize() {
		return mTail.size();
	}
	
	public int getMaxSize() {
		return mMaxSize;
	}
	
	public int getMove() {
		return mMove;
	}
	
	public AgentInfo getInfo() {
		return mInfo;
	}
	
	public TurnState getTurnState() {
		return mTurnState;
	}
	
	public void setTurnState(TurnState st) {
		mTurnState = st;
	}
	
	public Collection<Vec> getTail() {
		return Collections.unmodifiableCollection(mTail);
	}
	
	public Vec getPos() {
		return mTail.get(0);
	}
	
	public AIStrategy getAILogic() {
		return mAILogic;
	}
	
	public void setAILogic(AIStrategy s) {
		mAILogic = s;
	}
	
	private TurnState mTurnState = TurnState.Ready;
	private int mAmountMoved = 0;
	
	private DataBattle mBoard;
	private Team mTeam;
	private int mMaxSize;
	private int mMove;
	private ArrayList<Vec> mTail = new ArrayList<Vec>();
	private AgentInfo mInfo;
	private AIStrategy mAILogic;
}

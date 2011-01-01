package game;

import util.*;

public interface AIStrategy {
	public AIAction solve(AIController c, Agent a);
	
	public static class BasicAttackAIStrategy implements AIStrategy {
		public AIAction solve(AIController c, Agent a) {
			System.out.println("BasicAttackStrategy: evaluating...");
			AgentInfo.Ability ab = a.getInfo().getAbilities()[0];
			//
			AIController.Path bestPath = null; //min move, max size, attack
			boolean bestCanAttack = false; //can the best path attack yet?
			int desiredMove = a.getMaxSize() - a.getSize(); //how much move to get to full size
			//
			for (Agent othera : c.getTarget().getAgents()) {
				if (othera.getTeam() != a.getTeam()) {
					for (Vec v : othera.getTail()) {
						AIController.Path path = c.Astar(c.getTile(a.getPos()), c.getTile(v), ab.range());
						if (path != null) {
							boolean canAttack = path.Move <= a.getMove();
							
							//short circuit, if you can kill something do so
							if (ab.damage() >= othera.getSize() && canAttack) {
								bestPath = path;
								break;
							}
							
							if (bestPath == null) { //no path? this must be the best!
								bestPath = path;
							} else if (canAttack) { 
								if (bestCanAttack) { //best can attack, do I have more optimal move?
									if (bestPath.Move < desiredMove) { //wants more move
										if (path.Move > bestPath.Move) { //I have more move, I win
											bestPath = path;
										}
									} else { //no more move, am I closer to optimal move, without going under?
										if (path.Move < bestPath.Move && path.Move > desiredMove) {
											bestPath = path;
										}
									}
								} else {			//can attack, best can't, I win
									bestPath = path;
									bestCanAttack = true;
								}
							} else if (!bestCanAttack) { //neither can attack
								if (path.Move < bestPath.Move) { //shortest path to enemy
									bestPath = path;
								}
							}
						}
					}
				}
			}

			AIAction act = new AIAction();
			act.addSelect();
			//found path?
			if (bestPath != null) {
				System.out.println("Path found!");
				{
					System.out.println("Path stats:\n\tMove:" + bestPath.Move + "\n\tRange:" + bestPath.Range);
					AIController.Path.Node n = bestPath.Start;
					while (n != null) {
						System.out.println("\t<" + n.Tile.Pos.getX() + ", " + n.Tile.Pos.getY() + 
								"> Range to goal: " + n.RangeToGoal);
						n = n.Next;
					}
				}
				//debug show path
				for (AIController.Path.Node nd = bestPath.Start; nd != null; nd = nd.Next)
					act.addDebugPath(nd.Tile.Pos);
				
				AIController.Path.Node n = bestPath.Start;
				//move
				for (int i = 0; i < Math.min(bestPath.Move, a.getMove()); ++i) {
					act.addMove(n.Tile.Pos);
					n = n.Next;
				}
				if (bestPath.Move <= a.getMove()) {
					//get square to attack if in range
					while (n.Next != null)
						n = n.Next;
					act.addAttack(ab, n.Tile.Pos);
				} else {
					//show proxy attack, but don't attack
					act.addShowAttack(ab);
				}
			}
			act.addDone();
			return act;
		}
	}
}

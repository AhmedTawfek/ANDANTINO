import java.util.HashMap;

public class TranspositionTable {

	public static HashMap<Integer, TTEntry> TT;
	public static int errors;
	public static int hits;
	public static Play alphaBetaTT(State s, int depth, int alpha, int beta, boolean Max, int color) {
		int olda = alpha;
		TTEntry n = retrieve(s);
		long hashKey = s.hashCode >> shift;
		if (n != null && hashKey == n.key && n.depth >= depth) {
			hits++;
			if (n.typeOfValue == Type.EXACT) {
				return new Play(n.value, n.bestMove);
			} else if (n.typeOfValue == Type.LOWER) {
				alpha = Math.max(alpha, n.value);
			} else {
				beta = Math.min(beta, n.value);
			}
			if (alpha >= beta)
				return new Play(n.value, n.bestMove);
		}

		Search.allStates++;
		boolean winning = s.isWinning();
		if (winning || depth == 0) {
			if (winning)
				Search.winningStates++;
			return new Play(Search.evaluate(s, winning, color, depth), s.idx);
		}
		int score;
		Play bestPlay = null;
		if (Max) {
			score = Integer.MIN_VALUE;
			for (State succ : s.getSuccessors()) {
				Play play = alphaBetaTT(succ, depth - 1, alpha, beta, !Max, color);
				if (play.value > score) {
					score = play.value;
					bestPlay = new Play(score, succ.idx);
				}
				alpha = Math.max(alpha, score);
				if (beta <= alpha) {
					// System.out.println("pruned at depth " + depth);
					break;
				}
			}
		} else {
			score = Integer.MAX_VALUE;
			for (State succ : s.getSuccessors()) {
				Play play = alphaBetaTT(succ, depth - 1, alpha, beta, !Max, color);
				if (play.value < score) {
					score = play.value;
					bestPlay = new Play(score, succ.idx);
				}
				beta = Math.min(beta, score);
				if (beta <= alpha) {
					// System.out.println("pruned at depth " + depth);
					break;
				}
			}
		}
		Type flag;
		if (score <= olda)
			flag = Type.UPPER;
		else if (score >= beta)
			flag = Type.LOWER;
		else
			flag = Type.EXACT;

		store(s, bestPlay.pos, score, flag, depth);

		return bestPlay;
	}

	static int shift = 30;

	private static void store(State s, int pos, int score, Type flag, int depth) {
		// TODO Auto-generated method stub
		long hash = s.hashCode;
		int code = (int) (hash & ((1 << shift) - 1));
		long key = hash >> shift;
		TTEntry entry = new TTEntry(score, flag, pos, depth, key);
		TT.put(code, entry); // We ignore all errors and we consider the "always replace" scheme
	}

	private static TTEntry retrieve(State s) {
		long hash = s.hashCode;
		int code = (int) (hash & ((1 << shift) - 1));
		long key = hash >> shift;
		TTEntry t = TT.get(code);
		if (t != null && t.key != key)
			errors++;

		return t;
	}
}

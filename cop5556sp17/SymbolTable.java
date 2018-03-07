package cop5556sp17;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Stack;

import cop5556sp17.AST.Dec;

public class SymbolTable {

	// TODO add fields

	private int currentScope, nextScope;
	private Stack<Integer> scopeStack;
	private Map<String, List<SymbolTableEntry>> symbolTableEntry;

	/**
	 * to be called when block entered
	 */
	public void enterScope() {
		// TODO: IMPLEMENT THIS
		currentScope = nextScope++;
		scopeStack.push(currentScope);
	}

	/**
	 * leaves scope
	 */
	public void leaveScope() {
		// TODO: IMPLEMENT THIS
		currentScope = scopeStack.pop();

	}

	public boolean insert(String ident, Dec dec) {
		// TODO: IMPLEMENT THIS

		List<SymbolTableEntry> listSymbolTableEntry;

		try {

			if (symbolTableEntry.containsKey(ident)) {

				listSymbolTableEntry = symbolTableEntry.get(ident);

				ListIterator<SymbolTableEntry> listIterator = listSymbolTableEntry
						.listIterator();

				while (listIterator.hasNext()) {

					SymbolTableEntry nextEntry = listIterator.next();

					if (nextEntry.scope == currentScope)
						return false;
				}

				listSymbolTableEntry.add(0, new SymbolTableEntry(currentScope,
						dec));
				symbolTableEntry.put(ident, listSymbolTableEntry);

			} else {

				listSymbolTableEntry = new LinkedList<SymbolTableEntry>();
				listSymbolTableEntry.add(0, new SymbolTableEntry(currentScope,
						dec));

				symbolTableEntry.put(ident, listSymbolTableEntry);
			}
		} catch (Exception e) { // define exception

			return false;
		}

		return true;
	}

	public Dec lookup(String ident) {
		// TODO: IMPLEMENT THIS

		if (symbolTableEntry.containsKey(ident)) {

			List<SymbolTableEntry> list = symbolTableEntry.get(ident);
			;

			ListIterator<SymbolTableEntry> listIterator = list.listIterator();

			while (listIterator.hasNext()) {

				SymbolTableEntry nextEntry = listIterator.next();

				if (scopeStack.search(nextEntry.scope) != -1)
					return nextEntry.dec;

			}
		}
		return null;
	}

	public SymbolTable() {
		// TODO: IMPLEMENT THIS

		this.symbolTableEntry = new HashMap<String, List<SymbolTableEntry>>();
		this.currentScope = 0;
		this.nextScope = 0;
		this.scopeStack = new Stack<Integer>();
	}

	@Override
	public String toString() {
		// TODO: IMPLEMENT THIS
		return "SymbolTable [current scope=" + currentScope + "]";
	}

	public static class SymbolTableEntry {

		public final int scope;
		public final Dec dec;

		public SymbolTableEntry(int scope, Dec dec) {

			this.scope = scope;
			this.dec = dec;

		}

		@Override
		public String toString() {
			return "SymbolTableEntry [scope=" + scope + ", dec=" + dec + "]";
		}

	}

}

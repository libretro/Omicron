package org.github.msx80.crazyclassloader;

import java.util.function.Predicate;

/**
 * This class loader will not load certain classes, instead delegate to parent
 * class loader to do the job
 */
public class ExceptingClassLoader extends DynamicClassLoader {

	private Predicate<String> except;

	public ExceptingClassLoader(Predicate<String> except, BytesLoader... l) {
		super(l);
		this.except = except;
	}

	@Override
	protected byte[] loadNewClass(String name) {
		if (except.test(name)) {
            return null;
		}

		return super.loadNewClass(name);
	}
	

}

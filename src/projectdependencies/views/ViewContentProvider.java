/*
 * Copyright (c) 2011 Andrejs Jermakovics.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package projectdependencies.views;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

class ViewContentProvider implements IStructuredContentProvider,
		ITreeContentProvider
{
	private boolean showReferenced = true;
	private boolean showTransitive;

	ViewContentProvider()
	{
	}

	@Override
	public void inputChanged(final Viewer v, final Object oldInput, final Object newInput)
	{
	}

	@Override
	public void dispose()
	{
	}

	@Override
	public Object[] getElements(final Object parent)
	{
		return ResourcesPlugin.getWorkspace().getRoot().getProjects();
	}

	@Override
	public Object getParent(final Object child)
	{
		return null;
	}

	@Override
	public Object[] getChildren(final Object parent)
	{
		if (parent instanceof IProject)
		{
			IProject proj = (IProject) parent;

			return getRefs(proj);
		}

		return new Object[]{};
	}

	private IProject[] getRefs(IProject proj)
	{
		try
		{
			if( showReferenced )
				return proj.getReferencedProjects();
			else
				return proj.getReferencingProjects();
		}
		catch (CoreException e)
		{
			e.printStackTrace();
		}

		return new IProject[]{};
	}

	@Override
	public boolean hasChildren(final Object parent)
	{
		if (parent instanceof IWorkspaceRoot)
			return true;

		if( parent instanceof IProject )
		{
			return getRefs((IProject)parent).length != 0;
		}
		
		return false;
	}

	public void setShowReferenced(boolean referencedParam)
	{
		this.showReferenced = referencedParam;
	}

	public boolean isShowReferenced()
	{
		return this.showReferenced;
	}

	public void setShowTransitive(boolean showTransitiveParam)
	{
		this.showTransitive = showTransitiveParam;
	}

	public boolean isShowTransitive()
	{
		return showTransitive;
	}
}
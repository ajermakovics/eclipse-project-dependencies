/*
 * Copyright (c) 2011 Andrejs Jermakovics.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package projectdependencies.views;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.map.MultiValueMap;
import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

class ViewContentProviderJdt extends ViewContentProvider
{
	private IJavaModel javaModel;
	private MultiValueMap pkgProjects = new MultiValueMap();
	private Map<IJavaProject, List<IJavaProject>> imported = new HashMap<IJavaProject, List<IJavaProject>>();

	ViewContentProviderJdt()
	{
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		this.javaModel = JavaCore.create(workspaceRoot);
	}

	@Override
	public void dispose()
	{
	}

	@Override
	public Object[] getElements(final Object parent)
	{
		try
		{
			List<IJavaProject> projects = asList(javaModel.getJavaProjects());
			//openProjects(projects);
			return getDepInfos(projects, null).toArray();
		}
		catch (JavaModelException e)
		{
			return new Object[]{e};
		}
	}

	private void buildPackageProjectMap(List<IJavaProject> referencedparam) throws JavaModelException
	{
		for(IJavaProject proj: referencedparam)
		{
			if( pkgProjects.containsValue(proj) ) // already indexed
				continue;
			
			for(IPackageFragment pkg: proj.getPackageFragments())
			{
				if( pkg.getKind() != IPackageFragmentRoot.K_SOURCE )
					continue;

				if( pkg.isDefaultPackage() )
					continue;

				pkgProjects.put(pkg.getElementName(), proj);
			}
		}
	}

	@Override
	public Object[] getChildren(final Object parent)
	{
		IJavaProject proj = null;
		
		if(parent instanceof DepInfo)
		{
			DepInfo info = (DepInfo) parent;
			proj = (IJavaProject) info.to;
		}
		
		if( proj != null )
		{
			try
			{
				List<IJavaProject> referenced = getRefs(proj);
				buildPackageProjectMap(referenced);
				
				List<IJavaProject> actualRequired = getCachedRequired(proj);
				List<DepInfo> depInfos = getDepInfos(referenced, actualRequired);

				return depInfos.toArray();
			}
			catch (JavaModelException e)
			{
				return new Object[]{e};
			}
		}

		return new Object[]{};
	}

	private List<IJavaProject> getCachedRequired(IJavaProject projparam) throws JavaModelException
	{
		String key = projparam.getProject().getName();
		
		if( imported.containsKey(key) )
			return imported.get(projparam);
		
		List<IJavaProject> reqProjects = getRequired(projparam);
		imported.put(projparam, reqProjects);
		
		return reqProjects;
	}

	private List<DepInfo> getDepInfos(List<IJavaProject> refs, List<IJavaProject> actualRequired)
	{
		List<DepInfo> depInfos = new ArrayList<DepInfo>();
		
		for(IJavaProject ref: refs)
		{
			boolean isUsed = (actualRequired==null ? true : actualRequired.contains(ref));
			depInfos.add( new DepInfo(ref, isUsed) );
		}

		return depInfos;
	}

	public static class DepInfo
	{
		IJavaProject to;
		boolean isUsed = true;
		
		public DepInfo(IJavaProject toparam, boolean isUsedparam)
		{
			super();
			this.to = toparam;
			this.isUsed = isUsedparam;
		}
	}
	
	private List<IJavaProject> getRequired(IJavaProject proj) throws JavaModelException
	{
		Collection<IJavaProject> required = new HashSet<IJavaProject>();
	
		for(IPackageFragment pkg: proj.getPackageFragments())
		{
			if( pkg.getKind() != IPackageFragmentRoot.K_SOURCE )
				continue;
		
			for(ICompilationUnit unit: pkg.getCompilationUnits())
			{
				for(IImportDeclaration importDec: unit.getImports())
				{
					Collection<IJavaProject> otherProj = findProjectsForImport( importDec );
					
					if( otherProj != null )
					{
						required.addAll(otherProj);
					}
				}
			}
		}
		
		return new ArrayList<IJavaProject>(required);
	}

	@SuppressWarnings("unchecked")
	private Collection<IJavaProject> findProjectsForImport(IImportDeclaration importDecparam) throws JavaModelException
	{
		boolean isStatic = Flags.isStatic( importDecparam.getFlags() );
		String importStr = importDecparam.getElementName();
		String[] parts = StringUtils.split(importStr, '.');

		int packageEndIndex = parts.length - (isStatic ? 2 : 1); // ignore class name and method name from import

		if( packageEndIndex <= 0 )
		{
			System.out.println("Empty or default package: " + importStr);
			return null;
		}

		String subPkg = StringUtils.join(parts, ".", 0, packageEndIndex);
		
		if( pkgProjects.containsKey(subPkg) )
			return pkgProjects.getCollection(subPkg);

		return null;
	}

	private List<IJavaProject> getRefs(IJavaProject projparam) throws JavaModelException
	{
		String[] projNames;
		projNames = projparam.getRequiredProjectNames();

		List<IJavaProject> refs = new ArrayList<IJavaProject>();
		
		for(String name: projNames)
		{
			IJavaProject proj = javaModel.getJavaProject(name);
			refs.add(proj);
		}
		
		return refs;
	}

	@Override
	public boolean hasChildren(final Object parent)
	{
		if (parent instanceof IWorkspaceRoot)
			return true;

		Object project = parent;
		
		if(project instanceof DepInfo)
		{
			project = ((DepInfo) parent).to;
		}
		
		if( project instanceof IJavaProject )
		{
			try
			{
				IJavaProject proj = (IJavaProject)project;
				return proj.getRequiredProjectNames().length != 0;
			}
			catch (JavaModelException e)
			{
				e.printStackTrace();
			}
		}
		
		return false;
	}

	void openProjects(IJavaProject[] projects) throws JavaModelException
	{
		for (IJavaProject proj : projects)
		{
			if( ! proj.isOpen() )
				proj.open(new NullProgressMonitor());
		}
	}

}
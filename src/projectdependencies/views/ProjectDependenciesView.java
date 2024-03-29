/*
 * Copyright (c) 2011 Andrejs Jermakovics.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package projectdependencies.views;

import static projectdependencies.ProjectDependenciesActivator.getImageDescriptor;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.part.ViewPart;

public class ProjectDependenciesView extends ViewPart
{
	public static final String ID = "projectdependencies.views.ProjectDependenciesView";

	private TreeViewer viewer;
	private Action toggleDirection;
	private ViewContentProvider provider;

	private Action showTransitive;
	
	public ProjectDependenciesView()
	{
	}

	@Override
	public void createPartControl(final Composite parent)
	{
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);

		this.provider = new ViewContentProviderJdt();

		viewer.setContentProvider(provider);
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new ViewerSorter());
		viewer.setInput( ResourcesPlugin.getWorkspace().getRoot() );

		makeActions();

		contributeToActionBars();
		refreshMessage(provider.isShowReferenced());
	}

	private void contributeToActionBars()
	{
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(final IMenuManager manager)
	{
		manager.add(toggleDirection);
		//manager.add(showTransitive);
	}

	private void fillLocalToolBar(final IToolBarManager manager)
	{
		manager.add(toggleDirection);
		//manager.add(showTransitive);
	}

	private void makeActions()
	{
		toggleDirection = new Action()
		{
			@Override
			public void run()
			{
				provider.setShowReferenced(toggleDirection.isChecked());
				viewer.refresh();
				
				refreshMessage(provider.isShowReferenced());
			}
		};

		toggleDirection.setText("Project Uses/Used by");
		toggleDirection.setToolTipText( toggleDirection.getText() );
		toggleDirection.setChecked( provider.isShowReferenced() );
		toggleDirection.setImageDescriptor( getImageDescriptor("icons/showchild_mode.gif") );
		
		this.showTransitive = new Action()
		{
			@Override
			public void run()
			{
				provider.setShowTransitive(showTransitive.isChecked());
				viewer.refresh();
			}
		};

		showTransitive.setText("Show Transitive");
		showTransitive.setToolTipText( showTransitive.getText() );
		showTransitive.setChecked( provider.isShowTransitive() );
		showTransitive.setImageDescriptor( getImageDescriptor("icons/cp_order_obj.gif") );
	}

	@Override
	public void setFocus()
	{
		viewer.getControl().setFocus();
	}

	private void refreshMessage(boolean uses)
	{
		String message = "Project " + (uses?"Uses":"Used By");
		setPartName(message);
	}
}
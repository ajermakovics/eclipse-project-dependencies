package projectdependencies.views;

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

import projectdependencies.Activator;

public class ProjectDependenciesView extends ViewPart
{
	public static final String ID = "projectdependencies.views.ProjectDependenciesView";

	private TreeViewer viewer;
	private Action toggleDirection;

	private ViewContentProvider provider;
	
	public ProjectDependenciesView()
	{
	}

	@Override
	public void createPartControl(final Composite parent)
	{
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);

		this.provider = new ViewContentProvider();

		viewer.setContentProvider(provider);
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new ViewerSorter());
		viewer.setInput( ResourcesPlugin.getWorkspace().getRoot() );

		makeActions();

		contributeToActionBars();
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
	}

	private void fillLocalToolBar(final IToolBarManager manager)
	{
		manager.add(toggleDirection);
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
				
				boolean uses = provider.isShowReferenced();

				refreshMessage(uses);
			}
		};

		toggleDirection.setText("Project Uses/Used by");
		toggleDirection.setToolTipText("Project Uses/Used by");
		toggleDirection.setChecked( provider.isShowReferenced() );
		toggleDirection.setImageDescriptor( Activator.getImageDescriptor("icons/showchild_mode.gif") );
	}

	@Override
	public void setFocus()
	{
		viewer.getControl().setFocus();
	}

	private void refreshMessage(boolean uses)
	{
		String message = "Project " + (uses?"uses":"used by");
		getViewSite().getActionBars().getStatusLineManager().setMessage(message);
		viewer.getControl().setToolTipText(message);
	}
}
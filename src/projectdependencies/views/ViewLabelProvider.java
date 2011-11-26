package projectdependencies.views;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.WorkbenchLabelProvider;

class ViewLabelProvider extends LabelProvider
{
	private WorkbenchLabelProvider labelProvider;

	/**
	 * @param projectDependenciesViewparam
	 */
	ViewLabelProvider()
	{
		this.labelProvider = new WorkbenchLabelProvider();
	}

	@Override
	public String getText(final Object obj)
	{
		if( obj instanceof IProject )
		{
			return ((IProject) obj).getName();
		}

		return obj.toString();
	}

	@Override
	public Image getImage(final Object obj)
	{
		String imageKey = ISharedImages.IMG_OBJ_FOLDER;

		if (obj instanceof IProject)
		{
			return labelProvider.getImage(obj);
		}

		return PlatformUI.getWorkbench().getSharedImages().getImage(imageKey);
	}
}
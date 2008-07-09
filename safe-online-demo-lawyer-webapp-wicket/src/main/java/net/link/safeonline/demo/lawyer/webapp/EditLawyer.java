package net.link.safeonline.demo.lawyer.webapp;

import net.link.safeonline.demo.wicket.tools.RolesAllowed;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;

@RolesAllowed("baradmin")
public class EditLawyer extends Layout {

	private static final long serialVersionUID = 1L;

	public EditLawyer() {
		Lawyer lawyer = ((LawyerSession) getSession()).getLawyer();

		add(new AdminNavigationBorder("navigation"));
		add(new EditLawyerForm("editLawyerForm", lawyer));
		add(new Label<String>("headerTitle", "Edit lawyer"));
		final FeedbackPanel feedback = new FeedbackPanel("feedback");
		add(feedback);
	}

	public final class EditLawyerForm extends Form<Lawyer> {

		private static final long serialVersionUID = 1L;

		public EditLawyerForm(final String id, Lawyer lawyer) {
			// Construct form with no validation listener
            super(id, new CompoundPropertyModel<Lawyer>(lawyer));

			// Add text entry widget
            add(new Label<String>("safeonlineId", lawyer.getSafeonlineId()));
			add(new Label<String>("name", lawyer.getName()));
            add(new RequiredTextField<String>("bar"));
			add(new CheckBox("isLawyer"));
			add(new CheckBox("isAdmin"));
		}

		@Override
        public final void onSubmit() {
			// Get the lawyer from the form
			// Lawyer lawyer = (Lawyer) getModelObject();

			// TODO: Save the lawyer
			info("Lawyer saved");

		}
	}

}

package net.link.safeonline.demo.lawyer.webapp;

import net.link.safeonline.wicket.tools.RolesAllowed;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;

@RolesAllowed("baradmin")
public class FindLawyer extends Layout {

	private static final long serialVersionUID = 1L;

	public FindLawyer() {
		add(new Label("headerTitle", "Find a Lawyer"));
		add(new AdminNavigationBorder("navigation"));

		final FeedbackPanel feedback = new FeedbackPanel("feedback");
		add(feedback);

		SearchForm form = new SearchForm("findLawyerForm");
		add(form);

	}

	public final class SearchForm extends Form<SearchFormInputModel> {

		private static final long serialVersionUID = 1L;

		public SearchForm(final String id) {
			// Construct form with no validation listener
            super(id, new CompoundPropertyModel<SearchFormInputModel>(
                    new SearchFormInputModel()));

			// Add text entry widget
            add(new RequiredTextField<String>("safeonlineId"));
		}

		/**
		 * Show the resulting valid edit
		 */
		@Override
        public final void onSubmit() {
			// Construct a copy of the edited comment
			SearchFormInputModel model = getModelObject();
			String safeonlineId = model.getSafeonlineId();

			// call webservice to find lawyer
			// for now: dummy
			Lawyer lawyer = new Lawyer();

			lawyer.setSafeonlineId(safeonlineId);
			lawyer.setBar("Gent");
			lawyer.setName("Pierke Pierlala");
			lawyer.setLawyer(true);
			lawyer.setAdmin(false);
			lawyer.setSuspended(false);

			// show edit page
			setResponsePage(EditLawyer.class);

			// push lawyer to session
			((LawyerSession) getSession()).setLawyer(lawyer);

		}
	}

}

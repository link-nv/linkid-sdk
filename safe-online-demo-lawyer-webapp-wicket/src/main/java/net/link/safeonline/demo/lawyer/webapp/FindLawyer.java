package net.link.safeonline.demo.lawyer.webapp;

import net.link.safeonline.demo.wicket.tools.RolesAllowed;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.RequiredTextField;
import wicket.markup.html.panel.FeedbackPanel;
import wicket.model.CompoundPropertyModel;

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

	public final class SearchForm extends Form {

		private static final long serialVersionUID = 1L;

		public SearchForm(final String id) {
			// Construct form with no validation listener
			super(id, new CompoundPropertyModel(new SearchFormInputModel()));

			// Add text entry widget
			add(new RequiredTextField("safeonlineId"));
		}

		/**
		 * Show the resulting valid edit
		 */
		@Override
        public final void onSubmit() {
			// Construct a copy of the edited comment
			SearchFormInputModel model = (SearchFormInputModel) getModelObject();
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

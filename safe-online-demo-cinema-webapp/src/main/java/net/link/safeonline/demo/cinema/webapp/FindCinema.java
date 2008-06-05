package net.link.safeonline.demo.cinema.webapp;

import net.link.safeonline.demo.wicket.tools.RolesAllowed;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;

@RolesAllowed("baradmin")
public class FindCinema extends Layout {

	private static final long serialVersionUID = 1L;

	public FindCinema() {
		add(new Label("headerTitle", "Find a Cinema"));
		add(new AdminNavigationBorder("navigation"));

		final FeedbackPanel feedback = new FeedbackPanel("feedback");
		add(feedback);

		SearchForm form = new SearchForm("findCinemaForm");
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

			// call webservice to find cinema
			// for now: dummy
			Cinema cinema = new Cinema();

			cinema.setSafeonlineId(safeonlineId);
			cinema.setBar("Gent");
			cinema.setName("Pierke Pierlala");
			cinema.setCinema(true);
			cinema.setAdmin(false);
			cinema.setSuspended(false);

			// show edit page
			setResponsePage(EditCinema.class);

			// push cinema to session
			((CinemaSession) getSession()).setCinema(cinema);

		}
	}

}

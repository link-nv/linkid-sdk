package net.link.safeonline.demo.cinema.webapp;

import net.link.safeonline.demo.wicket.tools.RolesAllowed;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;

@RolesAllowed("baradmin")
public class EditCinema extends Layout {

	private static final long serialVersionUID = 1L;

	public EditCinema() {
		Cinema cinema = ((CinemaSession) getSession()).getCinema();

		add(new AdminNavigationBorder("navigation"));
		add(new EditCinemaForm("editCinemaForm", cinema));
		add(new Label("headerTitle", "Edit cinema"));
		final FeedbackPanel feedback = new FeedbackPanel("feedback");
		add(feedback);
	}

	public final class EditCinemaForm extends Form {

		private static final long serialVersionUID = 1L;

		public EditCinemaForm(final String id, Cinema cinema) {
			// Construct form with no validation listener
			super(id, new CompoundPropertyModel(cinema));

			// Add text entry widget
			add(new Label("safeonlineId", cinema.getSafeonlineId()));
			add(new Label("name", cinema.getName()));
			add(new RequiredTextField("bar"));
			add(new CheckBox("isCinema"));
			add(new CheckBox("isAdmin"));
		}

		@Override
        public final void onSubmit() {
			// Get the cinema from the form
			// Cinema cinema = (Cinema) getModelObject();

			// TODO: Save the cinema
			info("Cinema saved");

		}
	}

}

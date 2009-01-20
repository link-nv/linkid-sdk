package net.link.safeonline.demo.bank.webapp;

import net.link.safeonline.demo.bank.entity.BankUserEntity;
import net.link.safeonline.demo.bank.webapp.NewAccountPage.AccountForm;
import net.link.safeonline.wicket.web.ForceLogout;

import org.apache.wicket.Page;
import org.apache.wicket.feedback.IFeedbackMessageFilter;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;


/**
 * <h2>{@link AdminPage}<br>
 * <sub>Wicket page for managing bank users and their links to olas users.</sub></h2>
 * 
 * <p>
 * <i>Jun 20, 2008</i>
 * </p>
 * 
 * @author mbillemo
 */
@ForceLogout
public class AdminPage extends LayoutPage {

    private static final long serialVersionUID = 1L;


    /**
     * Assign components to the wicket IDs defined in the HTML.
     */
    public AdminPage() {

        add(new AdminForm("adminForm"));
    }


    /**
     * <h2>{@link AccountForm}<br>
     * <sub>Admin Form.</sub></h2>
     * 
     * <p>
     * <i>Jun 23, 2008</i>
     * </p>
     * 
     * @author mbillemo
     */
    class AdminForm extends Form<String> {

        private static final long serialVersionUID = 1L;

        Model<String>             bankId;
        ListView<BankUserEntity>  bankIdsList;

        Model<String>             name;
        Model<Boolean>            linked;
        Model<Boolean>            delete;

        TextField<String>         bankIdField;
        TextField<String>         nameField;
        CheckBox                  linkedField;
        CheckBox                  deleteField;

        Button                    submitButton;
        FeedbackPanel             feedbackPanel;


        public AdminForm(String id) {

            super(id);

            add(bankIdField = new TextField<String>("bankId", bankId = new Model<String>()));

            add(bankIdsList = new ListView<BankUserEntity>("bankIds", getUserService().getUsers()) {

                private static final long serialVersionUID = 1L;


                @Override
                public boolean isVisible() {

                    return !getList().isEmpty();
                }

                @Override
                protected void populateItem(final ListItem<BankUserEntity> item) {

                    item.add(new Link<String>("select") {

                        private static final long serialVersionUID = 1L;

                        {
                            add(new Label("bankId", item.getModelObject().getBankId()));
                        }


                        @Override
                        public void onClick() {

                            bankId.setObject(item.getModelObject().getBankId());
                            onSubmit();
                        }
                    });
                    item.add(new Label("name", item.getModelObject().getName()));
                }
            });

            add(nameField = new TextField<String>("name", name = new Model<String>()));
            add(linkedField = new CheckBox("linked", linked = new Model<Boolean>()));
            add(deleteField = new CheckBox("delete", delete = new Model<Boolean>()));

            add(submitButton = new Button("submit", new Model<String>("Search &gt;")));
            add(feedbackPanel = new FeedbackPanel("feedback", IFeedbackMessageFilter.ALL));

            bankIdField.setRequired(true);
            submitButton.setEscapeModelStrings(false);

            nameField.setVisible(false);
            linkedField.setVisible(false);
            deleteField.setVisible(false);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void onBeforeRender() {

            // Decide what submit text to show.
            if (bankIdField.isEnabled()) {
                bankIdsList.setVisibilityAllowed(true);

                submitButton.setModelObject("Search &gt;");
            }

            else {
                bankIdsList.setVisibilityAllowed(false);

                if (nameField.isVisible()) {
                    submitButton.setModelObject("Create &gt;");
                }

                else if (linkedField.isVisible()) {
                    if (linkedField.isEnabled()) {
                        submitButton.setModelObject("Apply &gt;");
                    } else {
                        submitButton.setModelObject("Return &lt;");
                    }
                }
            }

            feedbackPanel.setVisible(feedbackPanel.anyMessage());

            super.onBeforeRender();
        }

        @Override
        protected void onSubmit() {

            // Toggle field visibility & enabling depending on whether this is a search or not;
            // and if it is, what result it yields.

            if (bankIdField.isEnabled()) {
                // Submit was a search query.

                BankUserEntity bankUser = getUserService().getBankUser(bankId.getObject());
                if (bankUser == null) {
                    nameField.setVisible(true);
                    linkedField.setVisible(false);
                    deleteField.setVisible(false);

                    name.setObject(null);
                }

                else {
                    nameField.setVisible(false);
                    linkedField.setVisible(true);
                    deleteField.setVisible(true);

                    linked.setObject(bankUser.getOlasId() != null);
                    linkedField.setEnabled(linked.getObject() == true);

                    delete.setObject(false);
                }

                bankIdField.setEnabled(false);
            }

            else {
                // Submit was an apply/create/return.

                if (nameField.isVisible()) {
                    // Submit was a create.

                    getUserService().addUser(bankId.getObject(), name.getObject());
                }

                if (linkedField.isVisible() && linked.getObject() == false) {
                    // Submit was an apply & link was broken by unchecking the checkbox.

                    getUserService().unlinkOLASUser(getUserService().getBankUser(bankId.getObject()));
                }

                if (deleteField.isVisible() && delete.getObject() == true) {
                    // Submit was an apply & delete was checked.

                    getUserService().removeUser(getUserService().getBankUser(bankId.getObject()));
                }

                nameField.setVisible(false);
                linkedField.setVisible(false);
                deleteField.setVisible(false);

                bankIdField.setEnabled(true);
                bankIdsList.setList(getUserService().getUsers());
            }
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected String getHeaderTitle() {

        return "User Administration Page";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    String getPageLinkString() {

        return "Login Page";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Class<? extends Page> getPageLinkDestination() {

        return LoginPage.class;
    }
}

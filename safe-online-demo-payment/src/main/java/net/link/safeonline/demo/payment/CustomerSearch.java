package net.link.safeonline.demo.payment;

import javax.ejb.Local;

@Local
public interface CustomerSearch extends AbstractPaymentDataClient {

	String search();

}

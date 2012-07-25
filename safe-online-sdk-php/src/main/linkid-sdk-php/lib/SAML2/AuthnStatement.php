<?php

/**
 * Class representing a SAML 2 assertion AuthnStatement
 *
 * @package simpleSAMLphp
 * @version $Id$
 */
class SAML2_AuthnStatement {

	/**
	 * The session expiration timestamp.
	 *
	 * @var int|NULL
	 */
	private $sessionNotOnOrAfter;

	/**
	 * The session index for this user on the IdP.
	 *
	 * Contains NULL if no session index is present.
	 *
	 * @var string|NULL
	 */
	private $sessionIndex;


	/**
	 * The timestamp the user was authenticated, as an UNIX timestamp.
	 *
	 * @var int
	 */
	private $authnInstant;


	/**
	 * The authentication context for this assertion.
	 *
	 * @var string|NULL
	 */
	private $authnContext;

	/**
	 * The list of AuthenticatingAuthorities for this assertion.
	 *
	 * @var array
	 */
	private $AuthenticatingAuthority;

	/**
	 * Constructor
	 *
	 * @param DOMElement|NULL $xml  The input assertion.
	 */
	public function __construct(DOMElement $as = NULL) {

		if (!$as->hasAttribute('AuthnInstant')) {
			throw new Exception('Missing required AuthnInstant attribute on <saml:AuthnStatement>.');
		}
		$this->authnInstant = SimpleSAML_Utilities::parseSAML2Time($as->getAttribute('AuthnInstant'));

		if ($as->hasAttribute('SessionNotOnOrAfter')) {
			$this->sessionNotOnOrAfter = SimpleSAML_Utilities::parseSAML2Time($as->getAttribute('SessionNotOnOrAfter'));
		}

		if ($as->hasAttribute('SessionIndex')) {
			$this->sessionIndex = $as->getAttribute('SessionIndex');
		}

		$ac = SAML2_Utils::xpQuery($as, './saml_assertion:AuthnContext');
		if (empty($ac)) {
			throw new Exception('Missing required <saml:AuthnContext> in <saml:AuthnStatement>.');
		} elseif (count($ac) > 1) {
			throw new Exception('More than one <saml:AuthnContext> in <saml:AuthnStatement>.');
		}
		$ac = $ac[0];

		$accr = SAML2_Utils::xpQuery($ac, './saml_assertion:AuthnContextClassRef');
		if (empty($accr)) {
			$acdr = SAML2_Utils::xpQuery($ac, './saml_assertion:AuthnContextDeclRef');
			if (empty($acdr)) {
				throw new Exception('Neither <saml:AuthnContextClassRef> nor <saml:AuthnContextDeclRef> found in <saml:AuthnContext>.');
			} elseif (count($accr) > 1) {
				throw new Exception('More than one <saml:AuthnContextDeclRef> in <saml:AuthnContext>.');
			}
			$this->authnContext = trim($acdr[0]->textContent);
		} elseif (count($accr) > 1) {
			throw new Exception('More than one <saml:AuthnContextClassRef> in <saml:AuthnContext>.');
		} else {
			$this->authnContext = trim($accr[0]->textContent);
		}

		$this->AuthenticatingAuthority = SAML2_Utils::extractStrings($ac, './saml_assertion:AuthenticatingAuthority');
    }

    /**
     * Retrieve the session expiration timestamp.
     *
     * This function returns NULL if there are no restrictions on the
     * session lifetime.
     *
     * @return int|NULL  The latest timestamp this session is valid.
     */
    public function getSessionNotOnOrAfter() {

        return $this->sessionNotOnOrAfter;
    }

	/**
	 * Retrieve the session index of the user at the IdP.
	 *
	 * @return string|NULL  The session index of the user at the IdP.
	 */
	public function getSessionIndex() {

		return $this->sessionIndex;
	}

	/**
	 * Retrieve the AuthnInstant of the assertion.
	 *
	 * @return int|NULL  The timestamp the user was authenticated, or NULL if the user isn't authenticated.
	 */
	public function getAuthnInstant() {

		return $this->authnInstant;
	}

	/**
	 * Retrieve the authentication method used to authenticate the user.
	 *
	 * This will return NULL if no authentication statement was
	 * included in the assertion.
	 *
	 * @return string|NULL  The authentication method.
	 */
	public function getAuthnContext() {

		return $this->authnContext;
	}

	/**
	 * Retrieve the AuthenticatingAuthority.
	 *
	 *
	 * @return array
	 */
	public function getAuthenticatingAuthority() {

		return $this->AuthenticatingAuthority;
	}

}
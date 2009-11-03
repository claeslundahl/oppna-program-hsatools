package se.vgregion.kivtools.hriv.presentation;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import se.vgregion.kivtools.hriv.presentation.validation.CaptchaValidator;
import se.vgregion.kivtools.search.svc.ErrorReportingService;

public class ErrorReportingFlowSupportBeanTest {

  private ErrorReportingFlowSupportBean errorReportingFlowSupportBean;
  private ErrorReportingServiceMock errorReportingService;
  private CaptchaValidatorMock captchaValidator;

  @Before
  public void setUp() throws Exception {
    errorReportingService = new ErrorReportingServiceMock();
    captchaValidator = new CaptchaValidatorMock();
    errorReportingFlowSupportBean = new ErrorReportingFlowSupportBean();
    errorReportingFlowSupportBean.setErrorReportingService(errorReportingService);
    errorReportingFlowSupportBean.setCaptchaValidator(captchaValidator);
  }

  @Test
  public void testInstantiation() {
    ErrorReportingFlowSupportBean errorReportingFlowSupportBean = new ErrorReportingFlowSupportBean();
    assertNotNull(errorReportingFlowSupportBean);
  }

  @Test
  public void testReportErrorValid() {
    captchaValidator.valid = true;
    String result = errorReportingFlowSupportBean.reportError("TestDn", "Test", "TestDetailLink", "TestCaptchaChallenge", "TestCaptchaResponse", "TestRemoteAddress");
    assertEquals("success", result);
    assertEquals("TestDn", errorReportingService.dn);
    assertEquals("Test", errorReportingService.reportText);
    assertEquals("TestDetailLink", errorReportingService.detailLink);
    assertEquals("TestCaptchaChallenge", captchaValidator.captchaChallenge);
    assertEquals("TestCaptchaResponse", captchaValidator.captchaResponse);
    assertEquals("TestRemoteAddress", captchaValidator.remoteAddress);
  }

  @Test
  public void testReportErrorInvalid() {
    captchaValidator.valid = false;
    String result = errorReportingFlowSupportBean.reportError("TestDn", "Test", "TestDetailLink", "TestCaptchaChallenge", "TestCaptchaResponse", "TestRemoteAddr");
    assertEquals("failure", result);
  }

  private static class ErrorReportingServiceMock implements ErrorReportingService {
    private String dn;
    private String reportText;
    private String detailLink;

    @Override
    public void reportError(String dn, String reportText, String detailLink) {
      this.dn = dn;
      this.reportText = reportText;
      this.detailLink = detailLink;
    }
  }

  private static class CaptchaValidatorMock implements CaptchaValidator {
    private boolean valid;
    private String captchaChallenge;
    private String captchaResponse;
    private String remoteAddress;

    @Override
    public boolean validate(String captchaChallenge, String captchaResponse, String remoteAddress) {
      this.captchaChallenge = captchaChallenge;
      this.captchaResponse = captchaResponse;
      this.remoteAddress = remoteAddress;
      return valid;
    }
  }
}
package se.vgregion.kivtools.search.svc.ws.vardval;

import java.security.KeyStoreException;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.ws.BindingProvider;

import se.vgregion.kivtools.search.svc.ws.domain.vardval.GetVårdvalRequest;
import se.vgregion.kivtools.search.svc.ws.domain.vardval.GetVårdvalResponse;
import se.vgregion.kivtools.search.svc.ws.domain.vardval.IVårdvalService;
import se.vgregion.kivtools.search.svc.ws.domain.vardval.IVårdvalServiceSetVårdValVårdvalServiceErrorFaultFaultMessage;
import se.vgregion.kivtools.search.svc.ws.domain.vardval.ObjectFactory;
import se.vgregion.kivtools.search.svc.ws.domain.vardval.SetVårdvalRequest;
import se.vgregion.kivtools.search.svc.ws.domain.vardval.SetVårdvalResponse;
import se.vgregion.kivtools.search.svc.ws.domain.vardval.VårdvalEntry;
import se.vgregion.kivtools.search.svc.ws.domain.vardval.VårdvalService;

public class VardvalServiceImpl implements VardvalService {

  private ObjectFactory objectFactory = new ObjectFactory();
  private String webserviceEndpoint;

  public void setEndpoint(IVårdvalService service) throws KeyStoreException {
    BindingProvider bindingProvider = (BindingProvider) service;
    Map<String, Object> requestContext = bindingProvider.getRequestContext();
    requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, webserviceEndpoint);
  }

  public void setWebserviceEndpoint(String webserviceEndpoint) {
    this.webserviceEndpoint = webserviceEndpoint;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public VardvalInfo getVardval(String ssn) {
    GetVårdvalResponse response = getVardvalInfo(ssn);
    return generateVardvalInfo(response);
  }

  private VardvalInfo generateVardvalInfo(Object response) {
    VardvalInfo vardvalInfo = new VardvalInfo();
    JAXBElement<VårdvalEntry> currentVardval = null;
    JAXBElement<VårdvalEntry> upcomingVardval = null;
    // Check what sort of soap response
    if (response instanceof GetVårdvalResponse) {
      currentVardval = ((GetVårdvalResponse) response).getAktivtVårdval();
      upcomingVardval = ((GetVårdvalResponse) response).getKommandeVårdval();
    } else if (response instanceof SetVårdvalResponse) {
      currentVardval = ((SetVårdvalResponse) response).getAktivtVårdval();
      upcomingVardval = ((SetVårdvalResponse) response).getKommandeVårdval();
    }

    if (currentVardval != null && currentVardval.getValue() != null) {
      vardvalInfo.setCurrentHsaId(currentVardval.getValue().getVårdcentralHsaId());
      vardvalInfo.setCurrentValidFromDate(currentVardval.getValue().getGiltigFrån().toGregorianCalendar().getTime());
    }

    if (upcomingVardval != null && upcomingVardval.getValue() != null) {
      vardvalInfo.setUpcomingHsaId(upcomingVardval.getValue().getVårdcentralHsaId());
      vardvalInfo.setUpcomingValidFromDate(upcomingVardval.getValue().getGiltigFrån().toGregorianCalendar().getTime());
    }
    return vardvalInfo;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public VardvalInfo setVardval(String ssn, String hsaId, byte[] signature) throws IVårdvalServiceSetVårdValVårdvalServiceErrorFaultFaultMessage {
    IVårdvalService service = new VårdvalService().getBasicHttpBindingIVårdvalService();
    try {
      setEndpoint(service);
    } catch (KeyStoreException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    SetVårdvalRequest vardvalRequest = new SetVårdvalRequest();
    JAXBElement<String> soapSsn = objectFactory.createSetVårdvalRequestPersonnummer(ssn);
    JAXBElement<byte[]> soapSignature = objectFactory.createSetVårdvalRequestSigneringskod(signature);
    JAXBElement<String> soapHsaId = objectFactory.createSetVårdvalRequestVårdcentralHsaId(hsaId);
    vardvalRequest.setPersonnummer(soapSsn);
    vardvalRequest.setSigneringskod(soapSignature);
    vardvalRequest.setVårdcentralHsaId(soapHsaId);
    SetVårdvalResponse response = service.setVårdVal(vardvalRequest);
    return generateVardvalInfo(response);
  }

  /**
   * 
   * @param ssn - Social Security Number
   * @return - Soap response
   */
  private GetVårdvalResponse getVardvalInfo(String ssn) {

    IVårdvalService service = new VårdvalService().getBasicHttpBindingIVårdvalService();

    System.out.println(System.getProperty("javax.net.ssl.keyStore"));

    try {
      setEndpoint(service);
    } catch (KeyStoreException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    JAXBElement<String> soapSsn = objectFactory.createGetVårdvalRequestPersonnummer(ssn);
    GetVårdvalRequest getVardvalRequest = objectFactory.createGetVårdvalRequest();
    getVardvalRequest.setPersonnummer(soapSsn);
    GetVårdvalResponse getVardvalResponse = service.getVårdVal(getVardvalRequest);
    return getVardvalResponse;
  }

  public void setVardvalService(VårdvalService vardvalService) {
  }
}

/**
 * Copyright 2009 Västa Götalandsregionen
 *
 *   This library is free software; you can redistribute it and/or modify
 *   it under the terms of version 2.1 of the GNU Lesser General Public
 *   License as published by the Free Software Foundation.
 *
 *   This library is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public
 *   License along with this library; if not, write to the
 *   Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 *   Boston, MA 02111-1307  USA
 */
package se.vgregion.kivtools.search.svc.domain;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

import com.domainlanguage.time.TimeInterval;
import com.domainlanguage.time.TimePoint;

/**
 * Representation of a Person
 * 
 * @author hangy2
 *
 */
@SuppressWarnings("serial")
public class Person implements Serializable {
    
    private String dn;              // Distinuished Name (e.g. cn=hangy2,ou=Personal,o=VGR)
    private String cn;              // Common Name, Hela Namnet (e.g. rogul999)
    private String vgrId;           // vgr-id samma värde som cn (e.g. rogul999)
    private String hsaPersonIdentityNumber;     // Person-id (e.g. 196712085983)
    private String givenName;       // tilltalsnamn (e.g. Christina)
    private String sn;              // efternamn (e.g. Svensson)
    private String hsaMiddleName;   // Mellannamn (e.g. Anna)
    private String initials;        // Initialer (e.g. K R)
    private String hsaNickName;     // Smeknamn (e.g. Rolle)
    private String fullName;        // Fullständigt Namn (e.g. Christina Svensson)
    private List<String> vgrStrukturPersonDN;   // A list of dns to Units where this person is employed
                                                // e.g ou=Sandlådan,ou=Org,o=VGR
                                                //     ou=d� och nu,ou=Sandlådan,ou=Org,o=VGR
    private List<String> vgrOrgRel; // A list of HsaIdentities to the Units where the person is employed
                                    // e.g. SE2321000131-E000000000101, SE2321000131-E000000005974
    private List<String> vgrAnstform;  // Anställningsform (e.g. 1)

    private TimeInterval employmentPeriod;
//    private String hsaStartDate;    // Anställnings start datum (e.g. 19850226230000Z)
//    private String endStartDate;    // Anställnings start datum (e.g. 19850226230000Z)
    private String hsaIdentity;     // HSA identitet (e.g. SE2321000131-P000000101458)
    private String mail;            // E-postadress (e.g. jessica.isegran@vgregion.se)
    private List<String> hsaSpecialityName; // Specialitetskod klartext e.g. Klinisk cytologi , Klinisk patologi
    private List<String> hsaSpecialityCode; // Specialitetskod e.g. 1024 , 1032
    private List<String> vgrAO3kod;         // Ansvarsomr�des kod e.g. 602, 785
    private List<String> vgrAnsvarsnummer;  // Ansvarsnumer e.g. 1, 2 
    private List<String> hsaLanguageKnowledgeCode; // List of Languages that the person speaks e.g. PL, RO
    private List<String> hsaLanguageKnowledgeText; // List of Languages that the person speaks e.g. Polska, Romanska
    private String hsaTitle;        // Legitimerade Yrkesgrupper e.g Biomedicinsk analytiker
    private String hsaPersonPrescriptionCode; // Personlig kod till person med r�tt att f�rskriva l�kemedel
    private List<Employment> employments; // List of Employment objects
    

    public String getDn() {
        return dn;
    }



    public void setDn(String dn) {
        this.dn = dn;
    }



    public String getCn() {
        return cn;
    }



    public void setCn(String cn) {
        this.cn = cn;
    }



    public String getVgrId() {
        return vgrId;
    }



    public void setVgrId(String vgrId) {
        this.vgrId = vgrId;
    }



    public String getHsaPersonIdentityNumber() {
        return hsaPersonIdentityNumber;
    }



    public void setHsaPersonIdentityNumber(String hsaPersonIdentityNumber) {
        this.hsaPersonIdentityNumber = hsaPersonIdentityNumber;
    }



    public String getGivenName() {
        return givenName;
    }



    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }



    public String getSn() {
        return sn;
    }



    public void setSn(String sn) {
        this.sn = sn;
    }



    public String getHsaMiddleName() {
        return hsaMiddleName;
    }



    public void setHsaMiddleName(String hsaMiddleName) {
        this.hsaMiddleName = hsaMiddleName;
    }



    public String getInitials() {
        return initials;
    }



    public void setInitials(String initials) {
        this.initials = initials;
    }



    public String getHsaNickName() {
        return hsaNickName;
    }



    public void setHsaNickName(String hsaNickName) {
        this.hsaNickName = hsaNickName;
    }



    public String getFullName() {
        return fullName;
    }



    public void setFullName(String fullName) {
        this.fullName = fullName;
    }



    public List<String> getVgrStrukturPersonDN() {
        return vgrStrukturPersonDN;
    }



    public void setVgrStrukturPersonDN(List<String> vgrStrukturPersonDN) {
        this.vgrStrukturPersonDN = vgrStrukturPersonDN;
    }



    public List<String> getVgrOrgRel() {
        return vgrOrgRel;
    }



    public void setVgrOrgRel(List<String> vgrOrgRel) {
        this.vgrOrgRel = vgrOrgRel;
    }



    public List<String> getVgrAnstform() {
        return vgrAnstform;
    }



    public void setVgrAnstform(List<String> vgrAnstform) {
        this.vgrAnstform = vgrAnstform;
    }

//    public String getHsaStartDate() {
//        return hsaStartDate;
//    }
//
//    public void setHsaStartDate(String hsaStartDate) {
//        this.hsaStartDate = hsaStartDate;
//    }
//
//    public String getEndStartDate() {
//        return endStartDate;
//    }
//
//    public void setEndStartDate(String endStartDate) {
//        this.endStartDate = endStartDate;
//    }

    public String getHsaIdentity() {
        return hsaIdentity;
    }



    public void setHsaIdentity(String hsaIdentity) {
        this.hsaIdentity = hsaIdentity;
    }



    public String getMail() {
        return mail;
    }



    public void setMail(String mail) {
        this.mail = mail;
    }



    public List<String> getHsaSpecialityName() {
        return hsaSpecialityName;
    }



    public void setHsaSpecialityName(List<String> hsaSpecialityName) {
        this.hsaSpecialityName = hsaSpecialityName;
    }



    public List<String> getHsaSpecialityCode() {
        return hsaSpecialityCode;
    }



    public void setHsaSpecialityCode(List<String> hsaSpecialityCode) {
        this.hsaSpecialityCode = hsaSpecialityCode;
    }



    public List<String> getVgrAO3kod() {
        return vgrAO3kod;
    }



    public void setVgrAO3kod(List<String> vgrAO3kod) {
        this.vgrAO3kod = vgrAO3kod;
    }



    public List<String> getVgrAnsvarsnummer() {
        return vgrAnsvarsnummer;
    }



    public void setVgrAnsvarsnummer(List<String> vgrAnsvarsnummer) {
        this.vgrAnsvarsnummer = vgrAnsvarsnummer;
    }



    public List<String> getHsaLanguageKnowledgeCode() {
        return hsaLanguageKnowledgeCode;
    }



    public void setHsaLanguageKnowledgeCode(List<String> hsaLanguageKnowledgeCode) {
        this.hsaLanguageKnowledgeCode = hsaLanguageKnowledgeCode;
    }



    public List<String> getHsaLanguageKnowledgeText() {
        return hsaLanguageKnowledgeText;
    }



    public void setHsaLanguageKnowledgeText(List<String> hsaLanguageKnowledgeText) {
        this.hsaLanguageKnowledgeText = hsaLanguageKnowledgeText;
    }



    public String getHsaTitle() {
        return hsaTitle;
    }

    public void setHsaTitle(String hsaTitle) {
        this.hsaTitle = hsaTitle;
    }
    
    public List<Employment> getEmployments() {
        return employments;
    }


    public void setEmployments(List<Employment> employments) {
        this.employments = employments;
    }

    public TimeInterval getEmploymentPeriod() {
        return employmentPeriod;
    }

    public void setEmploymentPeriod(TimeInterval employmentPeriod) {
        this.employmentPeriod = employmentPeriod;
    }
    
    public void setEmploymentPeriod(TimePoint startDate, TimePoint stopDate) {
        setEmploymentPeriod(TimeInterval.over(startDate, stopDate));
    }


    public String getHsaPersonPrescriptionCode() {
        return hsaPersonPrescriptionCode;
    }


    public void setHsaPersonPrescriptionCode(String hsaPersonPrescriptionCode) {
        this.hsaPersonPrescriptionCode = hsaPersonPrescriptionCode;
    }    
}

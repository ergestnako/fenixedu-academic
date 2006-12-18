package net.sourceforge.fenixedu.domain.accounting;

public enum EventType {

    CANDIDACY_ENROLMENT,

    SCHOOL_REGISTRATION_CERTIFICATE_REQUEST,

    ENROLMENT_CERTIFICATE_REQUEST,

    APPROVEMENT_CERTIFICATE_REQUEST,

    DEGREE_FINALIZATION_CERTIFICATE_REQUEST,

    SCHOOL_REGISTRATION_DECLARATION_REQUEST,
    
    ENROLMENT_DECLARATION_REQUEST,
    
    GRATUITY,

    INSURANCE,

    DFA_REGISTRATION,

    ADMINISTRATIVE_OFFICE_FEE,
    
    ADMINISTRATIVE_OFFICE_FEE_INSURANCE;

    public String getName() {
	return name();
    }

    public String getQualifiedName() {
	return EventType.class.getSimpleName() + "." + name();
    }

    public String getFullyQualifiedName() {
	return EventType.class.getName() + "." + name();
    }
}

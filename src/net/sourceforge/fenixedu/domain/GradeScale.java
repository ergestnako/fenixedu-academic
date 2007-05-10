package net.sourceforge.fenixedu.domain;

import java.util.ResourceBundle;

import net.sourceforge.fenixedu.domain.curriculum.EnrollmentState;
import net.sourceforge.fenixedu.domain.exceptions.DomainException;
import net.sourceforge.fenixedu.util.EvaluationType;
import net.sourceforge.fenixedu.util.LanguageUtils;

public enum GradeScale {

    TYPE20 {
	@Override
	protected boolean checkFinal(final String mark) {
	    if (mark.equals(NA) || mark.equals(RE)) {
		return true;
	    }
	    try {
		Integer markValue = Integer.valueOf(mark);
		if ((markValue >= 10) && (markValue <= 20)) {
		    return true;
		} else {
		    return false;
		}
	    } catch (NumberFormatException e) {
		return false;
	    }
	}

	@Override
	protected boolean checkNotFinal(final String mark) {
	    if (mark.equals(NA) || mark.equals(RE)) {
		return true;
	    }
	    try {
		Double markValue = Double.valueOf(mark);
		if ((markValue >= 0) && (markValue <= 20)) {
		    return true;
		} else {
		    return false;
		}
	    } catch (NumberFormatException e) {
		return false;
	    }
	}
	
	@Override
	protected String qualify(final String grade) {
	    try {
		final int gradeValue = Integer.valueOf(grade);
		final ResourceBundle applicationResources = ResourceBundle.getBundle("resources.ApplicationResources", LanguageUtils.getLocale());

		if (18 <= gradeValue && gradeValue <= 20) {
		    return applicationResources.getString("label.grade.a");
		} else if (16 <= gradeValue && gradeValue <= 17) {
		    return applicationResources.getString("label.grade.b");
		} else if (14 <= gradeValue && gradeValue <= 15) {
		    return applicationResources.getString("label.grade.c");
		} else if (10 <= gradeValue && gradeValue <= 13) {
		    return applicationResources.getString("label.grade.d");
		} else {
		    throw new DomainException("GradeScale.unable.to.qualify.given.grade");
		}
	    } catch (NumberFormatException e) {
		throw new DomainException("GradeScale.unable.to.qualify.given.grade");
	    }
	}
	
	@Override
	protected boolean isNotEvaluated(String grade) {
	    if (grade == null) {
		return true;
	    }
	    
	    grade = grade.trim();
	    return grade.equals("") || grade.equals("0") || grade.equals(GradeScale.NA);
	}
	
	@Override
	protected boolean isNotApproved(final String grade) {
	    if (grade.equals(GradeScale.RE)) {
		return true;
	    }

	    try {
		return Integer.valueOf(grade) < 10;
	    } catch (NumberFormatException e) {
		throw new DomainException("GradeScale.unable.to.get.enrolment.state");
	    }
	}
	
	@Override
	protected boolean isApproved(String grade) {
	    try {
		final int gradeValue = Integer.valueOf(grade);
		return 10 <= gradeValue && gradeValue <= 20;
	    } catch (NumberFormatException e) {
		throw new DomainException("GradeScale.unable.to.get.enrolment.state");
	    }
	}
	
    },

    TYPE5 {
	@Override
	protected boolean checkFinal(final String mark) {
	    if (mark.equals(NA) || mark.equals(RE)) {
		return true;
	    }
	    try {
		Integer markValue = Integer.valueOf(mark);
		if ((markValue >= 3) && (markValue <= 5)) {
		    return true;
		} else {
		    return false;
		}
	    } catch (NumberFormatException e) {
		return false;
	    }
	}

	@Override
	protected boolean checkNotFinal(final String mark) {
	    if (mark.equals(NA) || mark.equals(RE)) {
		return true;
	    }
	    try {
		Double markValue = Double.valueOf(mark);
		if ((markValue >= 0) && (markValue <= 20)) {
		    return true;
		} else {
		    return false;
		}
	    } catch (NumberFormatException e) {
		return false;
	    }
	}
    
	@Override
	protected String qualify(final String grade) {
	    try {
		final int gradeValue = Integer.valueOf(grade);
		final ResourceBundle applicationResources = ResourceBundle.getBundle("resources.ApplicationResources", LanguageUtils.getLocale());

		if (gradeValue == 5) {
		    return applicationResources.getString("label.grade.a");
		} else if (gradeValue == 4) {
		    return applicationResources.getString("label.grade.b");
		} else if (gradeValue == 3) {
		    return applicationResources.getString("label.grade.c");
		} else {
		    throw new DomainException("GradeScale.unable.to.qualify.given.grade");
		}
	    } catch (NumberFormatException e) {
		throw new DomainException("GradeScale.unable.to.qualify.given.grade");
	    }
	}
	
	@Override
	protected boolean isNotEvaluated(String grade) {
	    if (grade == null) {
		return true;
	    }
	    
	    grade = grade.trim();
	    return grade.equals("") || grade.equals("0") || grade.equals(GradeScale.NA);
	}
	
	@Override
	protected boolean isNotApproved(String grade) {
	    if (grade.equals(GradeScale.RE)) {
		return true;
	    }

	    try {
		return Integer.valueOf(grade) < 3;
	    } catch (NumberFormatException e) {
		throw new DomainException("GradeScale.unable.to.get.enrolment.state");
	    }
	}
	
	@Override
	protected boolean isApproved(String grade) {
	    try {
		final int gradeValue = Integer.valueOf(grade);
		return 3 <= gradeValue && gradeValue <= 5;
	    } catch (NumberFormatException e) {
		throw new DomainException("GradeScale.unable.to.get.enrolment.state");
	    }
	}
	
    },

    TYPEAP {
	@Override
	protected boolean checkFinal(final String mark) {
	    if (mark.equals(NA) || mark.equals(RE) || mark.equals(AP)) {
		return true;
	    }
	    return false;
	}

	@Override
	protected boolean checkNotFinal(final String mark) {
	    if (mark.equals(NA) || mark.equals(RE) || mark.equals(AP)) {
		return true;
	    }
	    return false;
	}

	@Override
	protected String qualify(final String grade) {
	    final ResourceBundle applicationResources = ResourceBundle.getBundle("resources.ApplicationResources", LanguageUtils.getLocale());
	    if (grade.equals(AP)) {
		return applicationResources.getString("msg.approved");
	    } else if (grade.equals(RE)) {
		return applicationResources.getString("msg.notApproved");
	    } else if (grade.equals(NA)) {
		return applicationResources.getString("msg.notEvaluated");
	    } else {
		throw new DomainException("GradeScale.unable.to.qualify.given.grade");
	    }
	}
	
	@Override
	protected boolean isNotEvaluated(String grade) {
	    if (grade == null) {
		return true;
	    }
	    
	    grade = grade.trim();
	    return grade.equals("") || grade.equals("0") || grade.equals(GradeScale.NA);
	}
	
	@Override
	protected boolean isNotApproved(String grade) {
	    return grade.equals(GradeScale.RE);
	}
	
	@Override
	protected boolean isApproved(String grade) {
	    return grade.equals(GradeScale.AP);
	}
	
    };

    static final public String NA = "NA";
    static final public String RE = "RE";
    static final public String AP = "AP";

    public String getName() {
	return name();
    }

    public boolean isValid(String mark, EvaluationType evaluationType) {
	mark = mark.toUpperCase();
	if (EvaluationType.FINAL_TYPE.equals(evaluationType)) {
	    return checkFinal(mark);
	} else {
	    return checkNotFinal(mark);
	}

    }

    abstract protected boolean checkFinal(final String mark);

    abstract protected boolean checkNotFinal(final String mark);

    abstract protected String qualify(final String grade);
    
    abstract protected boolean isNotEvaluated(final String grade);

    abstract protected boolean isNotApproved(final String grade);

    abstract protected boolean isApproved(final String grade);

    final public String getQualifiedName(final String grade) {
	if (isApproved(grade)) {
	    return qualify(grade);
	} else {
	    throw new DomainException("GradeScale.unable.to.qualify.given.grade");
	}
    }

    final protected EnrollmentState getEnrolmentState(String grade) {
	if (isNotEvaluated(grade)) {
	    return EnrollmentState.NOT_EVALUATED;
	} else if (isNotApproved(grade)) {
	    return EnrollmentState.NOT_APROVED;
	} else if (isApproved(grade)) {
	    return EnrollmentState.APROVED;
	} else {
	    throw new DomainException("GradeScale.unable.to.get.enrolment.state");
	}
    }
    
}

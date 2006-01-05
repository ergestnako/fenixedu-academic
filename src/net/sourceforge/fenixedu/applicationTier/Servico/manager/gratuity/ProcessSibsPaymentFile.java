package net.sourceforge.fenixedu.applicationTier.Servico.manager.gratuity;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.sourceforge.fenixedu.applicationTier.IUserView;
import net.sourceforge.fenixedu.applicationTier.Servico.exceptions.FenixServiceException;
import net.sourceforge.fenixedu.applicationTier.Servico.exceptions.gratuity.masterDegree.DuplicateSibsPaymentFileProcessingServiceException;
import net.sourceforge.fenixedu.domain.DomainFactory;
import net.sourceforge.fenixedu.domain.IExecutionDegree;
import net.sourceforge.fenixedu.domain.IExecutionYear;
import net.sourceforge.fenixedu.domain.IGratuitySituation;
import net.sourceforge.fenixedu.domain.IGratuityValues;
import net.sourceforge.fenixedu.domain.IInsuranceValue;
import net.sourceforge.fenixedu.domain.IPerson;
import net.sourceforge.fenixedu.domain.IPersonAccount;
import net.sourceforge.fenixedu.domain.IStudent;
import net.sourceforge.fenixedu.domain.IStudentCurricularPlan;
import net.sourceforge.fenixedu.domain.degree.DegreeType;
import net.sourceforge.fenixedu.domain.gratuity.SibsPaymentStatus;
import net.sourceforge.fenixedu.domain.gratuity.SibsPaymentType;
import net.sourceforge.fenixedu.domain.gratuity.masterDegree.ISibsPaymentFile;
import net.sourceforge.fenixedu.domain.gratuity.masterDegree.ISibsPaymentFileEntry;
import net.sourceforge.fenixedu.domain.studentCurricularPlan.Specialization;
import net.sourceforge.fenixedu.domain.transactions.IInsuranceTransaction;
import net.sourceforge.fenixedu.domain.transactions.PaymentType;
import net.sourceforge.fenixedu.domain.transactions.TransactionType;
import net.sourceforge.fenixedu.persistenceTier.ExcepcaoPersistencia;
import net.sourceforge.fenixedu.persistenceTier.IPersistentGratuitySituation;
import net.sourceforge.fenixedu.persistenceTier.IPersistentInsuranceValue;
import net.sourceforge.fenixedu.persistenceTier.ISuportePersistente;
import net.sourceforge.fenixedu.persistenceTier.PersistenceSupportFactory;
import net.sourceforge.fenixedu.persistenceTier.gratuity.masterDegree.IPersistentSibsPaymentFileEntry;
import net.sourceforge.fenixedu.persistenceTier.transactions.IPersistentInsuranceTransaction;
import net.sourceforge.fenixedu.util.gratuity.fileParsers.sibs.SibsPaymentFileUtils;
import pt.utl.ist.berserk.logic.serviceManager.IService;

public class ProcessSibsPaymentFile implements IService {

    /**
     * Process sibs payment files and creates corresponding payment transactions
     * 
     * @param filename
     * @param fileEntries
     * @throws FenixServiceException
     * @throws ExcepcaoPersistencia
     */
    public void run(String filename, List fileEntries, IUserView userView) throws FenixServiceException,
            ExcepcaoPersistencia {

        if (filename.trim().length() == 0) {
            throw new DuplicateSibsPaymentFileProcessingServiceException(
                    "error.exception.duplicateSibsPaymentFileProcessing");

        }

        final ISuportePersistente sp = PersistenceSupportFactory.getDefaultPersistenceSupport();
        final ISibsPaymentFile storedPaymentFile = sp.getIPersistentSibsPaymentFile().readByFilename(
                filename);
        if (storedPaymentFile != null) {
            throw new DuplicateSibsPaymentFileProcessingServiceException(
                    "error.exception.duplicateSibsPaymentFileProcessing");
        }

        ISibsPaymentFile sibsPaymentFile = SibsPaymentFileUtils.buildPaymentFile(filename, fileEntries);

        buildTransactionsAndStoreFile(sp, sibsPaymentFile, userView);
    }

    private void buildTransactionsAndStoreFile(ISuportePersistente sp, ISibsPaymentFile sibsPaymentFile,
            IUserView userView) throws ExcepcaoPersistencia {

        List<ISibsPaymentFileEntry> sibsPaymentFileEntries = sibsPaymentFile.getSibsPaymentFileEntries();

        int totalPaymentEntries = sibsPaymentFileEntries.size();

        findDuplicatesAndMarkThem(sp, sibsPaymentFileEntries, totalPaymentEntries);

        // lets build transactions for the entries in file
        final IPersistentInsuranceTransaction insuranceTransactionDAO = sp
                .getIPersistentInsuranceTransaction();
        final IPersistentGratuitySituation gratuitySituationDAO = sp.getIPersistentGratuitySituation();
        final IPersistentInsuranceValue insuranceValueDAO = sp.getIPersistentInsuranceValue();
        for (int i = 0; i < totalPaymentEntries; i++) {

            ISibsPaymentFileEntry sibsPaymentFileEntry = sibsPaymentFileEntries.get(i);

            if (sibsPaymentFileEntry.getPaymentStatus().equals(SibsPaymentStatus.PROCESSED_PAYMENT) == false) {
                continue;
            }

            // Exception cases should be inserted here
            // e.g. SMS credit payments (i.e. cases where duplicate entry
            // checking is not required)
            // assuming the form:
            // if (isSmsPayment())) {
            // do specific code if any
            // }
            // else { do specific code to insurance and gratuities }

            // DegreeType should be changed in future to support Degree Student
            // gratuity
            IStudent student = sp.getIPersistentStudent().readStudentByNumberAndDegreeType(
                    sibsPaymentFileEntry.getStudentNumber(), DegreeType.MASTER_DEGREE);

            int year = sibsPaymentFileEntry.getYear().intValue();
            String executionYearName = year + "/" + (year + 1);
            IExecutionYear executionYear = sp.getIPersistentExecutionYear().readExecutionYearByName(
                    executionYearName);

            if (executionYear == null) {

                // Change status to be solved manually because we could not find
                // execution year
                sibsPaymentFileEntry.setPaymentStatus(SibsPaymentStatus.INVALID_EXECUTION_YEAR);
                continue;
            }

            IPerson responsiblePerson = sp.getIPessoaPersistente().lerPessoaPorUsername(
                    userView.getUtilizador());

            IPersonAccount personAccount = sp.getIPersistentPersonAccount().readByPerson(
                    student.getPerson().getIdInternal());
            if (personAccount == null) {
                personAccount = DomainFactory.makePersonAccount(student.getPerson());
            }

            if (sibsPaymentFileEntry.getPaymentType().equals(SibsPaymentType.INSURANCE)) {

                IInsuranceValue insuranceValue = insuranceValueDAO.readByExecutionYear(executionYear
                        .getIdInternal());

                List insuranceTransactionList = insuranceTransactionDAO
                        .readAllNonReimbursedByExecutionYearAndStudent(executionYear.getIdInternal(),
                                student.getIdInternal());

                if (insuranceTransactionList.size() > 0) {
                    sibsPaymentFileEntry.setPaymentStatus(SibsPaymentStatus.DUPLICATE_INSURANCE_PAYMENT);

                } else {

                    if (sibsPaymentFileEntry.getPayedValue().equals(insuranceValue.getAnnualValue()) == false) {
                        // the value payed is not valid
                        sibsPaymentFileEntry.setPaymentStatus(SibsPaymentStatus.INVALID_INSURANCE_VALUE);

                    } else {
                        // create the insurance transaction payment for the
                        // execution year
                        IInsuranceTransaction insuranceTransaction = DomainFactory
                                .makeInsuranceTransaction(sibsPaymentFileEntry.getPayedValue(),
                                        new Timestamp(new Date().getTime()), null, PaymentType.SIBS,
                                        TransactionType.INSURANCE_PAYMENT, new Boolean(false),
                                        responsiblePerson, personAccount, null, executionYear, student);

                        insuranceTransactionDAO.lockWrite(insuranceTransaction);
                    }
                }
                continue;
            }

            Specialization specialization = determineSpecialization(sibsPaymentFileEntry);

            // DegreeType should be changed in future to meet Degree gratuity
            // requirements
            List<IStudentCurricularPlan> studentCurricularPlanList = student.getStudentCurricularPlans();

            List<IExecutionDegree> executionDegrees = new ArrayList<IExecutionDegree>();
            List<IStudentCurricularPlan> studentCurricularPlans = new ArrayList<IStudentCurricularPlan>();
            for (IStudentCurricularPlan studentCurricularPlan : studentCurricularPlanList) {

                if (!studentCurricularPlan.getSpecialization().equals(specialization)) {
                    continue;
                }

                IExecutionDegree candidateExecutionDegree = sp.getIPersistentExecutionDegree()
                        .readByDegreeCurricularPlanAndExecutionYear(
                                studentCurricularPlan.getDegreeCurricularPlan().getName(),
                                studentCurricularPlan.getDegreeCurricularPlan().getDegree().getSigla(),
                                executionYear.getYear());

                if (candidateExecutionDegree != null) {
                    executionDegrees.add(candidateExecutionDegree);
                    studentCurricularPlans.add(studentCurricularPlan);
                }
            }

            if ((executionDegrees.size() == 0) || (studentCurricularPlans.size() == 0)) {
                // Change status to be solved manually because we could not
                // decide the student curricular plan
                sibsPaymentFileEntry.setPaymentStatus(SibsPaymentStatus.INVALID_EXECUTION_DEGREE);
                continue;
            }

            if ((executionDegrees.size() > 1) || (studentCurricularPlans.size() > 1)) {
                // Change status to be solved manually because we could not
                // decide the student curricular plan
                sibsPaymentFileEntry
                        .setPaymentStatus(SibsPaymentStatus.UNABLE_TO_DETERMINE_STUDENT_CURRICULAR_PLAN);
                continue;
            }

            IExecutionDegree executionDegree = executionDegrees.get(0);
            IStudentCurricularPlan studentCurricularPlan = studentCurricularPlans.get(0);

            IGratuityValues gratuityValues = executionDegree.getGratuityValues();
            IGratuitySituation gratuitySituation = gratuitySituationDAO
                    .readGratuitySituatuionByStudentCurricularPlanAndGratuityValues(
                            studentCurricularPlan.getIdInternal(), gratuityValues.getIdInternal());
            if (gratuitySituation == null) {
                // Change status to be solved manually because the student does
                // not have a gratuity situation
                sibsPaymentFileEntry
                        .setPaymentStatus(SibsPaymentStatus.UNABLE_TO_DETERMINE_STUDENT_CURRICULAR_PLAN);
                continue;
            }

            TransactionType transactionType = bindSibsCodeTypeToTransactionCodeType(sibsPaymentFileEntry
                    .getPaymentType());

            DomainFactory.makeGratuityTransaction(sibsPaymentFileEntry.getPayedValue(), new Timestamp(
                    new Date().getTime()), null, PaymentType.SIBS, transactionType, new Boolean(false),
                    responsiblePerson, personAccount, null, gratuitySituation);

            // update remaining value of gratuity
            double oldRemainingValue = 0;
            if (gratuitySituation.getRemainingValue() != null) {
                oldRemainingValue = gratuitySituation.getRemainingValue().doubleValue();
            }
            double newRemainingValue = oldRemainingValue
                    - sibsPaymentFileEntry.getPayedValue().doubleValue();

            gratuitySituation.setRemainingValue(new Double(newRemainingValue));

        }

    }

    private void findDuplicatesAndMarkThem(ISuportePersistente sp,
            List<ISibsPaymentFileEntry> sibsPaymentFileEntries, int totalPaymentEntries)
            throws ExcepcaoPersistencia {
        for (int i = 0; i < totalPaymentEntries; i++) {

            ISibsPaymentFileEntry sibsPaymentFileEntry = sibsPaymentFileEntries.get(i);

            if (sibsPaymentFileEntry.getPaymentStatus().equals(SibsPaymentStatus.NOT_PROCESSED_PAYMENT)) {

                sibsPaymentFileEntry.setPaymentStatus(SibsPaymentStatus.PROCESSED_PAYMENT);

                // Exception cases should be inserted here
                // e.g. SMS credit payments (i.e. cases where duplicate entry
                // checking is not required)
                // assuming the form:
                // if (isSmsPayment())) {
                // do specific code if any
                // }
                // else { do specific code to insurance and gratuities }

                markDuplicateGratuityAndInsurancePayments(sp, sibsPaymentFileEntry,
                        sibsPaymentFileEntries, totalPaymentEntries, i);
            }
        }
    }

    private TransactionType bindSibsCodeTypeToTransactionCodeType(SibsPaymentType sibsPaymentType) {
        // in future if codes change too much, the binding table should be
        // loaded from a config file
        TransactionType transactionType = null;

        if (sibsPaymentType.equals(SibsPaymentType.INSURANCE)) {

            transactionType = TransactionType.INSURANCE_PAYMENT;

        } else if ((sibsPaymentType.equals(SibsPaymentType.MASTER_DEGREE_GRATUTITY_FIRST_PHASE))
                || (sibsPaymentType.equals(SibsPaymentType.SPECIALIZATION_GRATUTITY_FIRST_PHASE))) {

            transactionType = TransactionType.GRATUITY_FIRST_PHASE_PAYMENT;

        } else if ((sibsPaymentType.equals(SibsPaymentType.MASTER_DEGREE_GRATUTITY_SECOND_PHASE))
                || (sibsPaymentType.equals(SibsPaymentType.SPECIALIZATION_GRATUTITY_SECOND_PHASE))) {

            transactionType = TransactionType.GRATUITY_SECOND_PHASE_PAYMENT;

        } else if ((sibsPaymentType.equals(SibsPaymentType.MASTER_DEGREE_GRATUTITY_TOTAL))
                || (sibsPaymentType.equals(SibsPaymentType.SPECIALIZATION_GRATUTITY_TOTAL))) {

            transactionType = TransactionType.GRATUITY_FULL_PAYMENT;
        }

        return transactionType;
    }

    private void markDuplicateGratuityAndInsurancePayments(ISuportePersistente sp,
            ISibsPaymentFileEntry sibsPaymentFileEntry, List sibsPaymentFileEntries,
            int totalPaymentEntries, int currentIndex) throws ExcepcaoPersistencia {

        // first check if the gratuity or insurance payment is repeated inside
        // the file
        for (int j = currentIndex + 1; j < totalPaymentEntries; j++) {
            ISibsPaymentFileEntry duplicatePaymentCandidate = (ISibsPaymentFileEntry) sibsPaymentFileEntries
                    .get(j);

            if (sibsPaymentFileEntry.getYear().equals(duplicatePaymentCandidate.getYear())
                    && sibsPaymentFileEntry.getStudentNumber().equals(
                            duplicatePaymentCandidate.getStudentNumber())
                    && sibsPaymentFileEntry.getPaymentType().equals(
                            duplicatePaymentCandidate.getPaymentType())) {

                if (sibsPaymentFileEntry.getPaymentType().equals(SibsPaymentType.INSURANCE)) {

                    sibsPaymentFileEntry.setPaymentStatus(SibsPaymentStatus.DUPLICATE_INSURANCE_PAYMENT);
                } else {
                    sibsPaymentFileEntry.setPaymentStatus(SibsPaymentStatus.DUPLICATE_GRATUITY_PAYMENT);
                }

            }
        }

        if (sibsPaymentFileEntry.getPaymentStatus()
                .equals(SibsPaymentStatus.DUPLICATE_INSURANCE_PAYMENT)
                || sibsPaymentFileEntry.getPaymentStatus().equals(
                        SibsPaymentStatus.DUPLICATE_GRATUITY_PAYMENT)) {
            // the entry is already marked
            return;
        }

        // next check if the gratuity or insurance payment is repeated in
        // database
        final IPersistentSibsPaymentFileEntry sibsPaymentFileEntryDAO = sp
                .getIPersistentSibsPaymentFileEntry();
        List sibsPaymentFileEntryList = sibsPaymentFileEntryDAO
                .readByYearAndStudentNumberAndPaymentType(sibsPaymentFileEntry.getYear(),
                        sibsPaymentFileEntry.getStudentNumber(), sibsPaymentFileEntry.getPaymentType());

        if (sibsPaymentFileEntryList.size() > 0) {
            if (sibsPaymentFileEntry.getPaymentType().equals(SibsPaymentType.INSURANCE)) {
                sibsPaymentFileEntry.setPaymentStatus(SibsPaymentStatus.DUPLICATE_INSURANCE_PAYMENT);
            } else {
                sibsPaymentFileEntry.setPaymentStatus(SibsPaymentStatus.DUPLICATE_GRATUITY_PAYMENT);
            }
        }
    }

    private Specialization determineSpecialization(ISibsPaymentFileEntry sibsPaymentFileEntry) {
        // if sibs payment codes change to much in future this logic should be
        // moved to a config file
        SibsPaymentType sibsPaymentType = sibsPaymentFileEntry.getPaymentType();
        if (sibsPaymentType.equals(SibsPaymentType.SPECIALIZATION_GRATUTITY_TOTAL)
                || sibsPaymentType.equals(SibsPaymentType.SPECIALIZATION_GRATUTITY_FIRST_PHASE)
                || sibsPaymentType.equals(SibsPaymentType.SPECIALIZATION_GRATUTITY_SECOND_PHASE)) {
            return Specialization.SPECIALIZATION;
        }
        return Specialization.MASTER_DEGREE;
        // degree code goes here
    }

}

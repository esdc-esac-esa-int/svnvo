package esavo.uws.jobs.utils;

public enum UwsJobChangeType {
	StarTime,
	EndTime,
	ExecDuration,
	DestructionTime,
	AddedErrorSummary,
	SetRunIdentifier,
	SetQuote,
	ExecutionPhase,
	Location,
	UpdatedAllParameters //requires to remove current persistent information and save the new one

}

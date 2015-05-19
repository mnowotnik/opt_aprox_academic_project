package opt;

public class Decision {

	public final InputArgs inputArgs;
	public final Objectives objectives;
	public final Report report;
	public final Parameters parameters;

	public Decision(InputArgs inputArgs, Objectives objectives, Report report, Parameters parameters) {
		super();
		this.inputArgs = inputArgs;
		this.report = report;
		this.objectives = objectives;
		this.parameters = parameters;
	}

}

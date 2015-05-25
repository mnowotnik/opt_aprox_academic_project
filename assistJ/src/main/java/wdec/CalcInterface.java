package wdec;

import java.util.List;

import opt.Decision;

public interface CalcInterface
{
	public void fillTextFields(Decision decisions);
	public void addDecisions(List<Decision> decisions);
	public void calcGlow();
}
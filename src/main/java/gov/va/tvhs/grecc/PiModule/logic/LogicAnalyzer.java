package gov.va.tvhs.grecc.PiModule.logic;

import java.io.Serializable;

import gov.va.tvhs.grecc.PiModule.core.Constants;

/**
 * Abstract parent class of the BackwardLogicAnalyzer and ForwardLogicAnalyzer
 * classes. The class contains the fields used by the subclasses.
 * 
 * @author gobbelgt - Jul 25, 2017
 *
 */
abstract public class LogicAnalyzer implements Serializable
{
	private static final long serialVersionUID = -4185771073049044015L;

	/*
	 * Make this static so that both BackwardLogicAnalyzer subclass instances and
	 * ForwardLogicAnalyzer subclass instances are working based on the same array
	 */
	protected static Constants.ContextMark[] boundaryMarks;
	protected boolean isMarking;
	protected int currentIndex;
	protected int remainingContextLength;

}

package wdec;

import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import opt.Decision;
import opt.Solver;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.effect.Glow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

public class GuiController implements Initializable, CalcInterface
{

	CalcInterface afterCalc = this;
	double glowLevel;
	boolean glowUp;
	List<Decision> decisions;
	
	@FXML
	private Button calculateButton;

	@FXML
	private TextField cashTextField;

	@FXML
	private TextField debtTextField;

	@FXML
	private TextField periodTextField;

	@FXML
	private TextField volumeTextField;

	@FXML
	private TextField qualityTextField;

	@FXML
	private TextField tvTextField;

	@FXML
	private TextField internetTextField;

	@FXML
	private TextField magazinesTextField;

	@FXML
	private TextField priceTextField;

	@FXML
	private TextField loanTextField;

	@FXML
	private TextField instalmentTextField;
	
	@FXML
	private TextField unitPriceTextField;
	
	@FXML
	private TextField grossSalesIncomeTextField;
	
	@FXML
	private TextField primeCostsTextField;
	
	@FXML
	private TextField salesIncomeTextField;
	
	@FXML
	private TextField realNetIncomeTextField;
	
	@FXML
	private TextField wdecNetIncomeTextField;
	
	@FXML
	private TextField riskTextField;
	
	@FXML
	private Pane calculatingInfo;
	
	@FXML
	private LineChart<Number, Number> lineChart;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1)
	{		
		lineChart.setLegendVisible(false);
		
		calculateButton.setOnAction(new EventHandler<ActionEvent>()
		{

			@Override
			public void handle(ActionEvent event)
			{
				calculatingInfo.setVisible(true);
				calculateButton.setDisable(true);
				
				int debt = Integer.parseInt(debtTextField.getText());
				int period = Integer.parseInt(periodTextField.getText());
				int cash = Integer.parseInt(cashTextField.getText());
				
				glowLevel = 0.0;
				calculatingInfo.setEffect(new Glow(0.0));
				glowUp = true;
				
				new CalculatingThread(debt, period, cash, afterCalc);
			}

		});
}

	@Override
	public void fillTextFields(Decision decision)
	{	
		volumeTextField.setText(Integer.toString(decision.inputArgs.volume));
		qualityTextField.setText(Integer.toString(decision.inputArgs.quality));
		tvTextField.setText(Double.toString(decision.inputArgs.ads.tv));
		internetTextField.setText(Double.toString(decision.inputArgs.ads.internet));
		magazinesTextField.setText(Double.toString(decision.inputArgs.ads.warehouse));
		priceTextField.setText(Integer.toString(decision.inputArgs.price));
		loanTextField.setText(Integer.toString(decision.inputArgs.loan));
		instalmentTextField.setText(Integer.toString(decision.inputArgs.instalment));
		
		double formattedUnitPrice = new BigDecimal(decision.parameters.unitPrice ).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
		unitPriceTextField.setText(Double.toString(formattedUnitPrice));
		grossSalesIncomeTextField.setText(Double.toString(decision.report.grossSalesIncome));
		primeCostsTextField.setText(Double.toString(decision.report.primeCosts));
		salesIncomeTextField.setText(Double.toString(decision.report.salesIncome));

		double risk = 1.0 - decision.objectives.percSold;
		double formattedRisk = new BigDecimal(risk).setScale(5, BigDecimal.ROUND_HALF_UP).doubleValue();
		formattedRisk = formattedRisk * 100;
		
		realNetIncomeTextField.setText(Double.toString(decision.objectives.netIncome));
		
		double wdecIncome = Solver.convertToWdecIncome(decision.objectives.netIncome,
				decision.inputArgs.instalment);
		wdecNetIncomeTextField.setText(Double.toString(Math.round(wdecIncome)));

		riskTextField.setText(Double.toString(formattedRisk)+'%');
	}

	@Override
	public void calcGlow()
	{
		if(glowUp)
		{
			glowLevel = glowLevel + 0.01;
			if(glowLevel >= 1.0)
				{
					glowUp = false;
				}
		}
		else
		{
			glowLevel = glowLevel - 0.01;
			if(glowLevel <= 0.0) glowUp = true;
		}
		calculatingInfo.setEffect(new Glow(glowLevel));
	}

	@Override
	public void addDecisions(List<Decision> decisions)
	{
		
		lineChart.getData().clear();
		
		lineChart.getXAxis().setAutoRanging(true);
        lineChart.getYAxis().setAutoRanging(true);
		
		XYChart.Series<Number,Number> series1 = new XYChart.Series<Number, Number>();

		List<Node> nodeList = new ArrayList<Node>();
		List<Decision> filteredDecisionList = new ArrayList<Decision>();
		int intTemp = 0;
		
		for (Decision decision : decisions) {
			double income = decision.report.salesIncome;
			double risk = 1.0 - decision.objectives.percSold;
			double formattedRisk = new BigDecimal(risk).setScale(7, BigDecimal.ROUND_HALF_UP).doubleValue();
			formattedRisk = formattedRisk * 100;
			
			if(risk <= 0.001)
			{
				XYChart.Data<Number,Number> dataTemp = new XYChart.Data<Number, Number>(risk, income);
				series1.getData().add(dataTemp);
				intTemp++;
				filteredDecisionList.add(decision);
			}
		}

		lineChart.getData().add(series1);

		for(int i=0;i<intTemp;i++)
		{
			nodeList.add(lineChart.getData().get(0).getData().get(i).getNode());
		}
		
		setOnMouseEventsOnSeries(nodeList,filteredDecisionList);
		
		calculatingInfo.setVisible(false);
		calculateButton.setDisable(false);	
	}

	private void setOnMouseEventsOnSeries(List<Node> nodeList, List<Decision> filteredDecisionList)
	{
		for(int i=0;i<nodeList.size();i++){
			final int temp = i;
		 nodeList.get(i).setOnMouseClicked(new EventHandler<MouseEvent>() {
			 
	            @Override
	            public void handle(MouseEvent t) {
	               
	            	fillTextFields(filteredDecisionList.get(temp));
	            	
	            	
	            }
	        });
		}
		
	}
	
}
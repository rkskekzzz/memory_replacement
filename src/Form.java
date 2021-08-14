package memoryreplacement;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;

import memoryreplacement.Page.STATUS;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JPanel;
import java.awt.Component;

public class Form extends JFrame {

	private JFrame frame;
	private JPanel chartPanel;
	private JTextField randomTextField;
	private JTextField frameField;
	private JTextArea dataField;
	private JTextArea analysisField;
	private JTextArea additionalDataField;
	private JComboBox comboBox;
	private JButton saveButton;
	private JButton ranButton;
	private JButton runButton;
	private JLabel lblRandomText;
	private JLabel lblFrame;
	private JLabel lblPolicy;
	private JLabel lblPageFaultRate;
	private JLabel lblAddtionalData;
	private JScrollPane sp1;
	private JScrollPane sp2;
	private JScrollPane sp3;

	public int index = 0; // 메모리 크기
	public int hit = 0; // hit 횟수
	public int migration = 0; // migration 횟수
	public int fault = 0; // fault 횟수
	public int focus = 0; // 메모리에 올라와있는 페이지 개수
	public int cursor = 0; // 문자열에서 다음 실행될 위치

	Deque<Page> deque = new ArrayDeque<Page>(); // FIFO 구현시 사용할 stack
	List<Page> list = new ArrayList<Page>(); // OPTIMAL, LRU, LFU, MFU 구현시 사용할 arraylist
	HashMap<Character, Integer> map = new HashMap<Character, Integer>(); // LFU, MFU 구현시 참조 횟수를 count하는 hashmap

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Form window = new Form();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Form() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */

	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 699, 568);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		lblPolicy = new JLabel("Policy");
		lblPolicy.setBounds(33, 10, 57, 15);
		frame.getContentPane().add(lblPolicy);

		comboBox = new JComboBox();
		comboBox.setBounds(12, 35, 91, 21);
		frame.getContentPane().add(comboBox);

		comboBox.addItem("FIFO");
		comboBox.addItem("OPTIMAL");
		comboBox.addItem("LRU");
		comboBox.addItem("LFU");
		comboBox.addItem("MFU");

		randomTextField = new JTextField();
		randomTextField.setBounds(115, 35, 250, 21);
		frame.getContentPane().add(randomTextField);
		randomTextField.setColumns(10);

		ranButton = new JButton("Random");
		ranButton.setBounds(436, 19, 79, 37);
		frame.getContentPane().add(ranButton);
		ranButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Random rd = new Random();

				int count = rd.nextInt(45) + 5;
				StringBuilder sb = new StringBuilder();

				for (int i = 0; i < count; i++) {
					sb.append((char) (rd.nextInt(25) + 65));
				}

				randomTextField.setText(sb.toString());
			}
		});

		saveButton = new JButton("Save");
		saveButton.setBounds(603, 19, 72, 37);
		frame.getContentPane().add(saveButton);

		analysisField = new JTextArea();
		sp2 = new JScrollPane(analysisField);
		sp2.setBounds(436, 66, 239, 144);
		frame.getContentPane().add(sp2);

		runButton = new JButton("Run");
		runButton.setBounds(527, 19, 64, 37);
		frame.getContentPane().add(runButton);

		frameField = new JTextField();
		frameField.setColumns(10);
		frameField.setBounds(367, 35, 57, 21);
		frame.getContentPane().add(frameField);
		frameField.setText("4");
		dataField = new JTextArea();
		sp1 = new JScrollPane(dataField);
		sp1.setBounds(12, 66, 412, 311);
		frame.getContentPane().add(sp1);

		lblRandomText = new JLabel("Random Text");
		lblRandomText.setBounds(198, 10, 120, 15);
		frame.getContentPane().add(lblRandomText);

		lblFrame = new JLabel("Frame");
		lblFrame.setBounds(372, 10, 41, 15);
		frame.getContentPane().add(lblFrame);

		chartPanel = new JPanel();
		chartPanel.setBounds(436, 220, 239, 132);
		frame.getContentPane().add(chartPanel);

		lblPageFaultRate = new JLabel("Page Fault Rate(%)  ");
		lblPageFaultRate.setBounds(436, 362, 239, 15);
		frame.getContentPane().add(lblPageFaultRate);

		additionalDataField = new JTextArea();

		sp3 = new JScrollPane(additionalDataField);
		sp3.setBounds(12, 414, 659, 99);
		frame.getContentPane().add(sp3);

		lblAddtionalData = new JLabel("Additional Data");
		lblAddtionalData.setBounds(12, 387, 239, 15);
		frame.getContentPane().add(lblAddtionalData);

		runButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				initializeData(); // 클래스 전역변수 초기화 함수
				STATUS status;
				String data = randomTextField.getText(); // RandomTextField의 Text를 String으로 저장
				if (frameField.getText() == null) // Frame이 비어있으면 실행 중지
					return;

				index = Integer.parseInt(frameField.getText()); // frame으로 부터 받아온 메모리 크기
				for (int i = 0; i < data.length(); i++) { // data 길이만큼 반복
					int j = 0; // 현재 메모리의 데티터 개수
					char element = data.charAt(i);

					if (comboBox.getSelectedItem() == "FIFO") { // FIFO 알고리즘
						status = OperateFIFO(element);

						for (Page temp : deque) { // 1번 공간에 메모리 현황 표시
							char comp = temp.data;
							j++;
							dataField.append(String.valueOf(comp) + " ");
						}
						for (int k = j; k < index; k++) // 빈 메모리 공간은 " - "로 표시
							dataField.append(" - ");
						dataField.append("\n");

					} else {
						if (comboBox.getSelectedItem() == "OPTIMAL") // OPTIMAL 알고리즘
							status = OperateOPTIMAL(element);
						else if (comboBox.getSelectedItem() == "LRU") // LRU 알고리즘
							status = OperateLRU(element);
						else if (comboBox.getSelectedItem() == "LFU") // LFU 알고리즘
							status = OperateLFU(element);
						else // MFU 알고리즘
							status = OperateMFU(element);

						for (Page temp : list) { // 1번 공간에 메모리 현황 표시
							char comp = temp.data;
							j++;
							dataField.append(String.valueOf(comp) + " ");
						}
						for (int k = j; k < index; k++) // 빈 메모리 공간은 " - "로 표시
							dataField.append(" - ");
						dataField.append("\n");
					}

					// 2번 공간에 페이지별 상태 표시
					analysisField.append("data " + element + " is " + (status == STATUS.HIT ? "HIT\n"
							: (status == STATUS.MIGRATION ? "MIGRATION\n" : "FAULT\n")));
				}

				// 4번 공간에 추가 데이터 표시
				additionalDataField.append("\nfinal  ");
				printMap(); // LFU, MFU 사용시 만든 map print
				makeChart(); // 차트 만들기
				lblPageFaultRate.setText("Page Fault Rate(%)  " + (fault * 100 / (hit + fault)) + "%"); // 비율 계산
			}
		});

	}

	public void printMemory(Object o) {

	}

	public void makeChart() { // 차트 만들기
		PieDataset dataset = createDataset(); // PieDataset 생성

		JFreeChart chart = ChartFactory.createPieChart("", dataset, true, true, false); //차트 생성
		ChartPanel CP = new ChartPanel(chart) {  // 패널에 차트 추가
			public Dimension getPreferredSize() {
				return new Dimension(239, 130); // 기본 크기 설정
			}
		};
		chartPanel.add(CP);  // JPanel에 차트 추가
		chartPanel.validate();
	}
	private PieDataset createDataset() {
		DefaultPieDataset dataset = new DefaultPieDataset(); // dataset 설정
		dataset.setValue("Hit", hit);
		dataset.setValue("Fault", fault - migration);
		dataset.setValue("Migration", migration);
		return dataset;
	}

	public void initializeData() {
		dataField.setText("");
		analysisField.setText("");
		additionalDataField.setText("");
		deque.clear();
		list.clear();
		map.clear();
		chartPanel.removeAll();
		hit = 0;
		migration = 0;
		fault = 0;
		focus = 0;
		cursor = 0;
	}

	

	public Page.STATUS OperateLFU(char data) {
		cursor++;
		String str = randomTextField.getText();
		Page newPage = new Page();

		int minindex, min;
		for (Page temp : list) {
			char comp = temp.data;
			if (comp == data) {
				hit++;
				map.put(data, map.get(data) + 1);
				return STATUS.HIT;
			}
		}
		newPage.pid = Page.CREATE_ID++;
		newPage.data = data;

		if (map.containsKey(data))
			map.put(data, map.get(data) + 1);
		else
			map.put(data, 1);

		if (focus < index) {
			focus++;
			list.add(newPage);
			fault++;
			return STATUS.PAGEFAULT;
		} else {

			minindex = 0;
			min = map.get(list.get(0).data);
			for (int i = 1; i < index; i++) {
				int savep = 0;
				int mincursor = 0;
				int datacursor = 0;
				if (map.get(list.get(i).data) < min) {
					minindex = i;
					min = map.get(list.get(i).data);
				} else if (map.get(list.get(i).data) == min) {

					for (int j = cursor - 2; j >= 0; j--) {
						if (str.charAt(j) == list.get(minindex).data) {
							mincursor = j;
							break;
						}
					}
					for (int j = cursor - 2; j >= 0; j--) {
						if (str.charAt(j) == list.get(i).data) {
							datacursor = j;
							savep = i;
							break;
						}
					}
					if (mincursor > datacursor)
						minindex = savep;
				}

			} // 70120304230321201701
			additionalDataField.append((migration + 1) + " migration\n");
			printMap();
			additionalDataField.append("--------------------------------------------------------------\n");
			list.set(minindex, newPage);
			fault++;
			migration++;
			return STATUS.MIGRATION;
		}

	}

	public Page.STATUS OperateMFU(char data) {
		cursor++;
		String str = randomTextField.getText();
		Page newPage = new Page();

		int maxindex, max;
		for (Page temp : list) {
			char comp = temp.data;
			if (comp == data) {
				hit++;
				map.put(data, map.get(data) + 1);
				return STATUS.HIT;
			}
		}
		newPage.pid = Page.CREATE_ID++;
		newPage.data = data;

		if (map.containsKey(data))
			map.put(data, map.get(data) + 1);
		else
			map.put(data, 1);

		if (focus < index) {
			focus++;
			list.add(newPage);
			fault++;
			return STATUS.PAGEFAULT;
		} else {

			maxindex = 0;
			max = map.get(list.get(0).data);
			for (int i = 1; i < index; i++) {
				int savep = 0;
				int mincursor = 0;
				int datacursor = 0;
				System.out.println("max : " + max + " new : " + map.get(list.get(i).data));
				if (map.get(list.get(i).data) > max) {
					maxindex = i;
					max = map.get(list.get(i).data);
				} else if (map.get(list.get(i).data) == max) {

					for (int j = cursor - 2; j >= 0; j--) {
						if (str.charAt(j) == list.get(maxindex).data) {
							mincursor = j;
							break;
						}
					}
					for (int j = cursor - 2; j >= 0; j--) {
						if (str.charAt(j) == list.get(i).data) {
							datacursor = j;
							savep = i;
							break;
						}
					}
					if (mincursor > datacursor)
						maxindex = savep;
				}

			} // 70120304230321201701
			additionalDataField.append((migration + 1) + " migration\n");
			printMap();
			additionalDataField.append("--------------------------------------------------------------\n");
			list.set(maxindex, newPage);
			fault++;
			migration++;
			return STATUS.MIGRATION;
		}
	}

	public Page.STATUS OperateLRU(char data) {
		cursor++;
		Page newPage = new Page();
		int uselessindex = 0;
		String str = randomTextField.getText();
		List<Character> history = new ArrayList<Character>();

		for (Page temp : list) {
			char comp = temp.data;
			if (comp == data) {
				hit++;
				return STATUS.HIT;
			}

		}
		newPage.pid = Page.CREATE_ID++;
		newPage.data = data;

		if (focus < index) {
			focus++;
			list.add(newPage);
			fault++;
			return STATUS.PAGEFAULT;
		} else {
			for (int j = cursor - 2; j >= 0; j--) {
				if (history.size() == index)
					break;
				if (history.contains(str.charAt(j)) == false) {
					history.add(str.charAt(j));
				}
			}

			char temp = history.get(index - 1);
			for (int i = 0; i < index; i++) {
				if (temp == list.get(i).data) {
					uselessindex = i;
				}
			}
			list.set(uselessindex, newPage);

			migration++;
			fault++;
			return STATUS.MIGRATION;
		}
	}

	public Page.STATUS OperateOPTIMAL(char data) {
		cursor++; // 선택될 페이지의 위치 ++
		Page newPage = new Page(); // 새 페이지 생성
		int maxindex = 0; // 페이지가 이후에 나오는 위치 index
		String str = randomTextField.getText(); // 앞의 페이지를 분석하기 위한 페이지 정보
		int[] arr = new int[index]; // 각 페이지가 다음에 나올 위치를 저장하는 배열
		Arrays.fill(arr, 999); // 배열의 모든 값을 최대값으로 설정

		for (Page temp : list) {
			char comp = temp.data;
			if (comp == data) {
				hit++;
				return STATUS.HIT;
			}

		}
		newPage.pid = Page.CREATE_ID++;
		newPage.data = data;

		if (focus < index) {
			focus++;
			list.add(newPage);
			fault++;
			return STATUS.PAGEFAULT;
		} else {
			for (int j = 0; j < index; j++) {
				for (int i = cursor; i < str.length(); i++) {
					if (arr[j] != 999)
						break;
					if (list.get(j).data == str.charAt(i)) 
						arr[j] = i - cursor;
				}
			}
			int max = arr[0];
			for (int k = 0; k < arr.length; k++) {
				if (arr[k] > max) {
					max = arr[k];
					maxindex = k;
				}
			}
			list.set(maxindex, newPage);
			migration++;
			fault++;
			return STATUS.MIGRATION;
		}
	}

	public Page.STATUS OperateFIFO(char data) {
		Page newPage = new Page();  //  새 페이지 생성

		for (Page temp : deque) {  // 메모리에 올라와 있는 페이지 순차 탐색
			char comp = temp.data;
			if (comp == data) {  // 만약 새로 들어온 페이지가 메모리에 있는 페이지라면 
				hit++; // hit count 증가
				return STATUS.HIT;   // HIT
			}
		}
		newPage.pid = Page.CREATE_ID++;  //  페이지 pid 설정
		newPage.data = data; // 페이지 정보 설정

		if (focus < index) { // 메모리에 있는 페이지가 메모리 크기보다 작다면
			focus++; // 메모리에 있는 페이지 개수 증가
			deque.add(newPage); // stack add
			fault++; // fault count 증가
			return STATUS.PAGEFAULT; // PAGEFAULT
		} else {  //  메모리가 꽉 찼다면 
			deque.remove(); // stack remove
			deque.add(newPage); // stack add
			migration++; // migration count 증가
			fault++; // fault count 증가
			return STATUS.MIGRATION;  // MIGRATION
		}

	}

	public void printMap() {
		Set<Map.Entry<Character, Integer>> entries = map.entrySet();
		additionalDataField.append("key, count : [ ");
		for (Map.Entry<Character, Integer> entry : entries) {
			additionalDataField.append("{\"" + entry.getKey() + "\", " + entry.getValue() + "}, ");
		}
		additionalDataField.append("]\n");
	}
}

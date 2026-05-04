-- V5 F28b: Add 5 new assessment scales (RIASEC / BIG5 / VALUES / STRESS / TEAM)
-- Uses INSERT IGNORE so re-running is idempotent.
-- Scale IDs 2-6 (MBTI is scale_id=1 from seed_assessments.sql / manual seed).

-- ─────────────────────────────────────────────────────────────────────────────
-- Scales
-- ─────────────────────────────────────────────────────────────────────────────
INSERT IGNORE INTO `assessment_scales`
  (scale_id, title, description, question_count, status, version, is_active)
VALUES
(2, 'RIASEC职业兴趣', 'Holland职业兴趣测评，探索你的实际型(R)、研究型(I)、艺术型(A)、社会型(S)、企业型(E)、常规型(C)倾向', 12, 'ACTIVE', 1, 1),
(3, '大五人格(BIG5)', '测量开放性(O)、尽责性(C)、外向性(E)、宜人性(A)、神经质(N)五大人格维度', 10, 'ACTIVE', 1, 1),
(4, '职业价值观', '了解你最看重的职业价值维度：成就感、安全感、自主性、社会服务、地位声望、多样挑战', 12, 'ACTIVE', 1, 1),
(5, '压力应对测评', '评估你在压力情境中的应对风格和情绪调节模式', 10, 'ACTIVE', 1, 1),
(6, '团队角色测评', '基于Belbin模型，识别你在团队中最自然的角色：领导者、推进者、协调者、创意者、执行者', 10, 'ACTIVE', 1, 1);

-- ─────────────────────────────────────────────────────────────────────────────
-- Scale 2: RIASEC  (12 questions, A/B choice per dimension pair)
-- Dimensions: R=实际型  I=研究型  A=艺术型  S=社会型  E=企业型  C=常规型
-- ─────────────────────────────────────────────────────────────────────────────
INSERT IGNORE INTO `assessment_questions`
  (scale_id, question_text, question_type, dimension_code, sort_order, is_active)
VALUES
(2, '你更喜欢哪种类型的工作？', 'CHOICE', 'R_I', 1, 1),
(2, '以下哪种活动更吸引你？', 'CHOICE', 'I_A', 2, 1),
(2, '你在空闲时间更愿意做什么？', 'CHOICE', 'A_S', 3, 1),
(2, '面对一个新项目，你更倾向于？', 'CHOICE', 'S_E', 4, 1),
(2, '你更擅长哪种工作方式？', 'CHOICE', 'E_C', 5, 1),
(2, '你更享受哪类任务？', 'CHOICE', 'C_R', 6, 1),
(2, '在团队中，你更喜欢承担什么角色？', 'CHOICE', 'R_S', 7, 1),
(2, '以下哪个方向更符合你的职业理想？', 'CHOICE', 'I_E', 8, 1),
(2, '你认为自己更具备哪种能力？', 'CHOICE', 'A_C', 9, 1),
(2, '工作中你更看重什么？', 'CHOICE', 'S_I', 10, 1),
(2, '你更倾向于哪种工作环境？', 'CHOICE', 'E_R', 11, 1),
(2, '面对数据和文件，你的态度是？', 'CHOICE', 'C_A', 12, 1);

-- RIASEC Options (each question: option A → first dimension, option B → second dimension)
-- Q1 R vs I
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '动手操作机器、设备或户外体力工作', 1.00, 'R'
FROM assessment_questions q WHERE q.scale_id=2 AND q.sort_order=1;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '研究、分析数据，解决复杂技术问题', 1.00, 'I'
FROM assessment_questions q WHERE q.scale_id=2 AND q.sort_order=1;

-- Q2 I vs A
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '阅读科学文献，做实验或推理分析', 1.00, 'I'
FROM assessment_questions q WHERE q.scale_id=2 AND q.sort_order=2;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '创作艺术、设计或写作表达', 1.00, 'A'
FROM assessment_questions q WHERE q.scale_id=2 AND q.sort_order=2;

-- Q3 A vs S
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '独自从事创意写作、绘画或音乐', 1.00, 'A'
FROM assessment_questions q WHERE q.scale_id=2 AND q.sort_order=3;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '帮助别人、辅导他人或社区服务', 1.00, 'S'
FROM assessment_questions q WHERE q.scale_id=2 AND q.sort_order=3;

-- Q4 S vs E
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '倾听朋友的烦恼，提供支持和建议', 1.00, 'S'
FROM assessment_questions q WHERE q.scale_id=2 AND q.sort_order=4;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '说服他人接受我的想法，带领团队', 1.00, 'E'
FROM assessment_questions q WHERE q.scale_id=2 AND q.sort_order=4;

-- Q5 E vs C
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '主导商业谈判或销售推广', 1.00, 'E'
FROM assessment_questions q WHERE q.scale_id=2 AND q.sort_order=5;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '整理档案、维护表格、制定流程', 1.00, 'C'
FROM assessment_questions q WHERE q.scale_id=2 AND q.sort_order=5;

-- Q6 C vs R
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '按固定程序处理数据或行政任务', 1.00, 'C'
FROM assessment_questions q WHERE q.scale_id=2 AND q.sort_order=6;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '维修电器、组装设备或参与建造', 1.00, 'R'
FROM assessment_questions q WHERE q.scale_id=2 AND q.sort_order=6;

-- Q7 R vs S
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '专注于技术执行，不太需要与人打交道', 1.00, 'R'
FROM assessment_questions q WHERE q.scale_id=2 AND q.sort_order=7;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '与人互动，教育培训或提供咨询', 1.00, 'S'
FROM assessment_questions q WHERE q.scale_id=2 AND q.sort_order=7;

-- Q8 I vs E
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '科研、技术或学术方向', 1.00, 'I'
FROM assessment_questions q WHERE q.scale_id=2 AND q.sort_order=8;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '创业、管理或商业运营方向', 1.00, 'E'
FROM assessment_questions q WHERE q.scale_id=2 AND q.sort_order=8;

-- Q9 A vs C
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '自由发挥创意，即兴创作', 1.00, 'A'
FROM assessment_questions q WHERE q.scale_id=2 AND q.sort_order=9;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '按规章制度精确执行，注重准确性', 1.00, 'C'
FROM assessment_questions q WHERE q.scale_id=2 AND q.sort_order=9;

-- Q10 S vs I
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '能帮助到他人，有社会价值', 1.00, 'S'
FROM assessment_questions q WHERE q.scale_id=2 AND q.sort_order=10;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '探索未知，获取新知识新发现', 1.00, 'I'
FROM assessment_questions q WHERE q.scale_id=2 AND q.sort_order=10;

-- Q11 E vs R
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '竞争性强、目标导向的商业环境', 1.00, 'E'
FROM assessment_questions q WHERE q.scale_id=2 AND q.sort_order=11;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '户外、实验室或工厂等动手操作环境', 1.00, 'R'
FROM assessment_questions q WHERE q.scale_id=2 AND q.sort_order=11;

-- Q12 C vs A
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '我喜欢把事情处理得井井有条', 1.00, 'C'
FROM assessment_questions q WHERE q.scale_id=2 AND q.sort_order=12;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '我更喜欢用创新方式打破常规', 1.00, 'A'
FROM assessment_questions q WHERE q.scale_id=2 AND q.sort_order=12;

-- ─────────────────────────────────────────────────────────────────────────────
-- Scale 3: BIG5 大五人格 (10 questions, A=target trait / B=opposing trait)
-- Dimensions: O=开放性  C=尽责性  E=外向性  A=宜人性  N=神经质
-- ─────────────────────────────────────────────────────────────────────────────
INSERT IGNORE INTO `assessment_questions`
  (scale_id, question_text, question_type, dimension_code, sort_order, is_active)
VALUES
(3, '面对新事物，你通常会？', 'CHOICE', 'O', 1, 1),
(3, '对于好奇心和想象力，你认为？', 'CHOICE', 'O', 2, 1),
(3, '完成一项任务时，你倾向于？', 'CHOICE', 'C', 3, 1),
(3, '你对截止日期的态度是？', 'CHOICE', 'C', 4, 1),
(3, '在社交聚会中，你通常是？', 'CHOICE', 'E', 5, 1),
(3, '独处还是与人为伴，你更倾向于？', 'CHOICE', 'E', 6, 1),
(3, '当朋友发生争执时，你会？', 'CHOICE', 'A', 7, 1),
(3, '对于他人的观点，你通常？', 'CHOICE', 'A', 8, 1),
(3, '面对压力时，你的感受是？', 'CHOICE', 'N', 9, 1),
(3, '情绪波动方面，你更倾向于？', 'CHOICE', 'N', 10, 1);

-- BIG5 Options
-- Q1 Openness
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '充满好奇，积极探索和尝试', 1.00, 'O'
FROM assessment_questions q WHERE q.scale_id=3 AND q.sort_order=1;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '倾向于熟悉的方式，不太愿意冒险改变', 1.00, 'O_LOW'
FROM assessment_questions q WHERE q.scale_id=3 AND q.sort_order=1;

-- Q2 Openness
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '我富有想象力，经常有创造性的想法', 1.00, 'O'
FROM assessment_questions q WHERE q.scale_id=3 AND q.sort_order=2;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '我更务实，专注于实际可行的事情', 1.00, 'O_LOW'
FROM assessment_questions q WHERE q.scale_id=3 AND q.sort_order=2;

-- Q3 Conscientiousness
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '提前计划，按步骤认真完成', 1.00, 'C'
FROM assessment_questions q WHERE q.scale_id=3 AND q.sort_order=3;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '随心所欲，做到差不多就好', 1.00, 'C_LOW'
FROM assessment_questions q WHERE q.scale_id=3 AND q.sort_order=3;

-- Q4 Conscientiousness
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '严格遵守，提前完成', 1.00, 'C'
FROM assessment_questions q WHERE q.scale_id=3 AND q.sort_order=4;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '常常拖到最后一刻', 1.00, 'C_LOW'
FROM assessment_questions q WHERE q.scale_id=3 AND q.sort_order=4;

-- Q5 Extraversion
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '活跃中心，主动结交新朋友', 1.00, 'E'
FROM assessment_questions q WHERE q.scale_id=3 AND q.sort_order=5;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '待在角落，等别人来搭话', 1.00, 'E_LOW'
FROM assessment_questions q WHERE q.scale_id=3 AND q.sort_order=5;

-- Q6 Extraversion
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '与人相处让我充满活力', 1.00, 'E'
FROM assessment_questions q WHERE q.scale_id=3 AND q.sort_order=6;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '独处让我恢复精力', 1.00, 'E_LOW'
FROM assessment_questions q WHERE q.scale_id=3 AND q.sort_order=6;

-- Q7 Agreeableness
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '尽力调解，维护和谐关系', 1.00, 'A'
FROM assessment_questions q WHERE q.scale_id=3 AND q.sort_order=7;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '坚持己见，直接表达立场', 1.00, 'A_LOW'
FROM assessment_questions q WHERE q.scale_id=3 AND q.sort_order=7;

-- Q8 Agreeableness
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '尊重并理解不同观点，愿意妥协', 1.00, 'A'
FROM assessment_questions q WHERE q.scale_id=3 AND q.sort_order=8;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '认为自己的判断通常更准确', 1.00, 'A_LOW'
FROM assessment_questions q WHERE q.scale_id=3 AND q.sort_order=8;

-- Q9 Neuroticism
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '容易感到紧张和焦虑', 1.00, 'N'
FROM assessment_questions q WHERE q.scale_id=3 AND q.sort_order=9;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '通常保持冷静，很少感到压力', 1.00, 'N_LOW'
FROM assessment_questions q WHERE q.scale_id=3 AND q.sort_order=9;

-- Q10 Neuroticism
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '情绪容易受外界影响，起伏较大', 1.00, 'N'
FROM assessment_questions q WHERE q.scale_id=3 AND q.sort_order=10;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '情绪稳定，能较好地自我调节', 1.00, 'N_LOW'
FROM assessment_questions q WHERE q.scale_id=3 AND q.sort_order=10;

-- ─────────────────────────────────────────────────────────────────────────────
-- Scale 4: 职业价值观 (12 questions)
-- Dimensions: ACHIEVE=成就感  SECURE=安全感  AUTONOMY=自主性  SERVICE=社会服务  STATUS=地位声望  VARIETY=多样挑战
-- ─────────────────────────────────────────────────────────────────────────────
INSERT IGNORE INTO `assessment_questions`
  (scale_id, question_text, question_type, dimension_code, sort_order, is_active)
VALUES
(4, '你认为工作中最重要的是？', 'CHOICE', 'ACHIEVE_SECURE', 1, 1),
(4, '在职业选择时，你更重视？', 'CHOICE', 'AUTONOMY_STATUS', 2, 1),
(4, '以下哪种工作回报更打动你？', 'CHOICE', 'SERVICE_VARIETY', 3, 1),
(4, '你更倾向于哪种工作状态？', 'CHOICE', 'ACHIEVE_AUTONOMY', 4, 1),
(4, '对你而言，理想工作最重要的特征是？', 'CHOICE', 'SECURE_SERVICE', 5, 1),
(4, '你更看重职业带来的哪种价值？', 'CHOICE', 'STATUS_VARIETY', 6, 1),
(4, '面对职业瓶颈，你会优先考虑？', 'CHOICE', 'ACHIEVE_STATUS', 7, 1),
(4, '以下哪种工作文化更吸引你？', 'CHOICE', 'AUTONOMY_VARIETY', 8, 1),
(4, '你希望你的职业能带来？', 'CHOICE', 'SERVICE_SECURE', 9, 1),
(4, '对工作满意度影响最大的因素是？', 'CHOICE', 'ACHIEVE_SERVICE', 10, 1),
(4, '你认为职业成功的标志是？', 'CHOICE', 'STATUS_SECURE', 11, 1),
(4, '让你长期留在一家公司的原因是？', 'CHOICE', 'AUTONOMY_SECURE', 12, 1);

-- Values Options (A=first dim, B=second dim per question)
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '实现有价值的目标，获得成就感', 1.00, 'ACHIEVE' FROM assessment_questions q WHERE q.scale_id=4 AND q.sort_order=1;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '稳定的收入和工作保障', 1.00, 'SECURE' FROM assessment_questions q WHERE q.scale_id=4 AND q.sort_order=1;

INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '自主决策，不受过多约束', 1.00, 'AUTONOMY' FROM assessment_questions q WHERE q.scale_id=4 AND q.sort_order=2;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '获得社会认可和行业地位', 1.00, 'STATUS' FROM assessment_questions q WHERE q.scale_id=4 AND q.sort_order=2;

INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '对他人或社会产生积极影响', 1.00, 'SERVICE' FROM assessment_questions q WHERE q.scale_id=4 AND q.sort_order=3;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '丰富多样的工作内容和挑战', 1.00, 'VARIETY' FROM assessment_questions q WHERE q.scale_id=4 AND q.sort_order=3;

INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '突破挑战，不断提升自己', 1.00, 'ACHIEVE' FROM assessment_questions q WHERE q.scale_id=4 AND q.sort_order=4;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '自己安排时间，灵活工作', 1.00, 'AUTONOMY' FROM assessment_questions q WHERE q.scale_id=4 AND q.sort_order=4;

INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '清晰的晋升通道和长期保障', 1.00, 'SECURE' FROM assessment_questions q WHERE q.scale_id=4 AND q.sort_order=5;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '能真正帮助到需要帮助的人', 1.00, 'SERVICE' FROM assessment_questions q WHERE q.scale_id=4 AND q.sort_order=5;

INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '令人尊敬的职位和头衔', 1.00, 'STATUS' FROM assessment_questions q WHERE q.scale_id=4 AND q.sort_order=6;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '每天都有新事物可以学习', 1.00, 'VARIETY' FROM assessment_questions q WHERE q.scale_id=4 AND q.sort_order=6;

INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '争取晋升，取得更大成就', 1.00, 'ACHIEVE' FROM assessment_questions q WHERE q.scale_id=4 AND q.sort_order=7;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '寻求行业内更有声望的职位', 1.00, 'STATUS' FROM assessment_questions q WHERE q.scale_id=4 AND q.sort_order=7;

INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '鼓励创新，给予充分自主权', 1.00, 'AUTONOMY' FROM assessment_questions q WHERE q.scale_id=4 AND q.sort_order=8;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '项目多样，不断给你新挑战', 1.00, 'VARIETY' FROM assessment_questions q WHERE q.scale_id=4 AND q.sort_order=8;

INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '帮助他人改变生活或解决问题', 1.00, 'SERVICE' FROM assessment_questions q WHERE q.scale_id=4 AND q.sort_order=9;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '稳定的收入和清晰的职业路径', 1.00, 'SECURE' FROM assessment_questions q WHERE q.scale_id=4 AND q.sort_order=9;

INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '达成挑战性目标，超越自我', 1.00, 'ACHIEVE' FROM assessment_questions q WHERE q.scale_id=4 AND q.sort_order=10;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '对社会或特定群体产生实质帮助', 1.00, 'SERVICE' FROM assessment_questions q WHERE q.scale_id=4 AND q.sort_order=10;

INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '在行业中建立名望，受人尊重', 1.00, 'STATUS' FROM assessment_questions q WHERE q.scale_id=4 AND q.sort_order=11;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '拥有稳定且有保障的工作', 1.00, 'SECURE' FROM assessment_questions q WHERE q.scale_id=4 AND q.sort_order=11;

INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '充分的自主权和创作空间', 1.00, 'AUTONOMY' FROM assessment_questions q WHERE q.scale_id=4 AND q.sort_order=12;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '良好的福利保障和工作稳定性', 1.00, 'SECURE' FROM assessment_questions q WHERE q.scale_id=4 AND q.sort_order=12;

-- ─────────────────────────────────────────────────────────────────────────────
-- Scale 5: 压力应对 (10 questions)
-- Dimensions: PROBLEM=问题解决型  EMOTION=情绪聚焦型  AVOID=回避型
-- ─────────────────────────────────────────────────────────────────────────────
INSERT IGNORE INTO `assessment_questions`
  (scale_id, question_text, question_type, dimension_code, sort_order, is_active)
VALUES
(5, '面对突发的工作压力，你的第一反应是？', 'CHOICE', 'STRESS_COPE', 1, 1),
(5, '当任务堆积让你喘不过气，你会？', 'CHOICE', 'STRESS_COPE', 2, 1),
(5, '处理高压项目时，你通常能否保持专注？', 'CHOICE', 'STRESS_COPE', 3, 1),
(5, '工作失误后，你的典型反应是？', 'CHOICE', 'STRESS_COPE', 4, 1),
(5, '长期处于高压环境中，你如何释放压力？', 'CHOICE', 'STRESS_COPE', 5, 1),
(5, '面对无法控制的外部因素，你通常？', 'CHOICE', 'STRESS_COPE', 6, 1),
(5, '截止日期临近但任务未完成时，你会？', 'CHOICE', 'STRESS_COPE', 7, 1),
(5, '压力下你的睡眠和作息情况？', 'CHOICE', 'STRESS_COPE', 8, 1),
(5, '对于"完美主义"，你的态度是？', 'CHOICE', 'STRESS_COPE', 9, 1),
(5, '当压力超出承受范围，你会？', 'CHOICE', 'STRESS_COPE', 10, 1);

-- Stress Options (A=problem-focused, B=emotion-focused, C=avoidance)
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '立即列出问题清单，制定解决步骤', 1.00, 'PROBLEM'
FROM assessment_questions q WHERE q.scale_id=5 AND q.sort_order=1;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '先平复情绪，和朋友倾诉或暂时放松', 1.00, 'EMOTION'
FROM assessment_questions q WHERE q.scale_id=5 AND q.sort_order=1;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'C', '暂时回避，等情绪平稳后再处理', 1.00, 'AVOID'
FROM assessment_questions q WHERE q.scale_id=5 AND q.sort_order=1;

INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '优先级排序，逐一击破', 1.00, 'PROBLEM'
FROM assessment_questions q WHERE q.scale_id=5 AND q.sort_order=2;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '向同事或家人寻求情感支持', 1.00, 'EMOTION'
FROM assessment_questions q WHERE q.scale_id=5 AND q.sort_order=2;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'C', '干脆暂停所有任务，先休息一天', 1.00, 'AVOID'
FROM assessment_questions q WHERE q.scale_id=5 AND q.sort_order=2;

INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '是的，压力反而让我更专注高效', 1.00, 'PROBLEM'
FROM assessment_questions q WHERE q.scale_id=5 AND q.sort_order=3;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '有时会，取决于我的情绪状态', 1.00, 'EMOTION'
FROM assessment_questions q WHERE q.scale_id=5 AND q.sort_order=3;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'C', '很难，压力让我分心或拖延', 1.00, 'AVOID'
FROM assessment_questions q WHERE q.scale_id=5 AND q.sort_order=3;

INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '分析原因，制定改进计划', 1.00, 'PROBLEM'
FROM assessment_questions q WHERE q.scale_id=5 AND q.sort_order=4;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '感到自责或内疚，需要时间平复', 1.00, 'EMOTION'
FROM assessment_questions q WHERE q.scale_id=5 AND q.sort_order=4;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'C', '尽量不去想，努力忘掉这件事', 1.00, 'AVOID'
FROM assessment_questions q WHERE q.scale_id=5 AND q.sort_order=4;

INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '运动、健康饮食，保持规律作息', 1.00, 'PROBLEM'
FROM assessment_questions q WHERE q.scale_id=5 AND q.sort_order=5;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '找朋友聊天、听音乐或观影放松', 1.00, 'EMOTION'
FROM assessment_questions q WHERE q.scale_id=5 AND q.sort_order=5;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'C', '沉迷游戏、刷手机或暴饮暴食', 1.00, 'AVOID'
FROM assessment_questions q WHERE q.scale_id=5 AND q.sort_order=5;

INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '聚焦可控部分，接受不可控部分', 1.00, 'PROBLEM'
FROM assessment_questions q WHERE q.scale_id=5 AND q.sort_order=6;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '感到沮丧，需要情感支持', 1.00, 'EMOTION'
FROM assessment_questions q WHERE q.scale_id=5 AND q.sort_order=6;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'C', '选择暂时不理会，走一步看一步', 1.00, 'AVOID'
FROM assessment_questions q WHERE q.scale_id=5 AND q.sort_order=6;

INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '加班加点冲刺完成', 1.00, 'PROBLEM'
FROM assessment_questions q WHERE q.scale_id=5 AND q.sort_order=7;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '向上级坦诚情况，寻求帮助', 1.00, 'EMOTION'
FROM assessment_questions q WHERE q.scale_id=5 AND q.sort_order=7;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'C', '先假装一切正常，拖延上交', 1.00, 'AVOID'
FROM assessment_questions q WHERE q.scale_id=5 AND q.sort_order=7;

INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '依然保持规律，压力不影响作息', 1.00, 'PROBLEM'
FROM assessment_questions q WHERE q.scale_id=5 AND q.sort_order=8;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '偶尔失眠，需要通过放松恢复', 1.00, 'EMOTION'
FROM assessment_questions q WHERE q.scale_id=5 AND q.sort_order=8;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'C', '经常失眠或睡眠质量极差', 1.00, 'AVOID'
FROM assessment_questions q WHERE q.scale_id=5 AND q.sort_order=8;

INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '追求高标准，但知道何时够好', 1.00, 'PROBLEM'
FROM assessment_questions q WHERE q.scale_id=5 AND q.sort_order=9;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '有时陷入完美主义，导致焦虑', 1.00, 'EMOTION'
FROM assessment_questions q WHERE q.scale_id=5 AND q.sort_order=9;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'C', '常常因为追求完美而迟迟不行动', 1.00, 'AVOID'
FROM assessment_questions q WHERE q.scale_id=5 AND q.sort_order=9;

INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '主动寻求专业帮助或导师指导', 1.00, 'PROBLEM'
FROM assessment_questions q WHERE q.scale_id=5 AND q.sort_order=10;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '向亲近的人倾诉，释放情绪', 1.00, 'EMOTION'
FROM assessment_questions q WHERE q.scale_id=5 AND q.sort_order=10;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'C', '选择麻木自己，什么都不做', 1.00, 'AVOID'
FROM assessment_questions q WHERE q.scale_id=5 AND q.sort_order=10;

-- ─────────────────────────────────────────────────────────────────────────────
-- Scale 6: 团队角色 (10 questions, Belbin-inspired)
-- Dimensions: LEADER=领导者  EXECUTOR=执行者  INNOVATOR=创意者  COORDINATOR=协调者
-- ─────────────────────────────────────────────────────────────────────────────
INSERT IGNORE INTO `assessment_questions`
  (scale_id, question_text, question_type, dimension_code, sort_order, is_active)
VALUES
(6, '在团队项目启动时，你通常会？', 'CHOICE', 'TEAM_ROLE', 1, 1),
(6, '当团队遇到内部冲突时，你倾向于？', 'CHOICE', 'TEAM_ROLE', 2, 1),
(6, '面对紧迫的截止日期，你会？', 'CHOICE', 'TEAM_ROLE', 3, 1),
(6, '在创意讨论会上，你最常做的是？', 'CHOICE', 'TEAM_ROLE', 4, 1),
(6, '当团队方向不明确时，你会？', 'CHOICE', 'TEAM_ROLE', 5, 1),
(6, '你最擅长在团队中做什么？', 'CHOICE', 'TEAM_ROLE', 6, 1),
(6, '项目评审时，你的角色通常是？', 'CHOICE', 'TEAM_ROLE', 7, 1),
(6, '团队成员遇到困难时，你会？', 'CHOICE', 'TEAM_ROLE', 8, 1),
(6, '在团队决策中，你最关注？', 'CHOICE', 'TEAM_ROLE', 9, 1),
(6, '团队庆功时，你最满意的是？', 'CHOICE', 'TEAM_ROLE', 10, 1);

-- Team Role Options
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '主导制定目标和分工计划', 1.00, 'LEADER' FROM assessment_questions q WHERE q.scale_id=6 AND q.sort_order=1;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '立即开始执行最紧急的任务', 1.00, 'EXECUTOR' FROM assessment_questions q WHERE q.scale_id=6 AND q.sort_order=1;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'C', '提出创新的项目切入角度', 1.00, 'INNOVATOR' FROM assessment_questions q WHERE q.scale_id=6 AND q.sort_order=1;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'D', '确保每个人的分工明确且协调', 1.00, 'COORDINATOR' FROM assessment_questions q WHERE q.scale_id=6 AND q.sort_order=1;

INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '直接介入，做出裁决', 1.00, 'LEADER' FROM assessment_questions q WHERE q.scale_id=6 AND q.sort_order=2;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '专注完成自己的任务，少管闲事', 1.00, 'EXECUTOR' FROM assessment_questions q WHERE q.scale_id=6 AND q.sort_order=2;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'C', '提出新的思路转移注意力', 1.00, 'INNOVATOR' FROM assessment_questions q WHERE q.scale_id=6 AND q.sort_order=2;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'D', '倾听各方，促成共识', 1.00, 'COORDINATOR' FROM assessment_questions q WHERE q.scale_id=6 AND q.sort_order=2;

INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '调配资源，确保团队如期完成', 1.00, 'LEADER' FROM assessment_questions q WHERE q.scale_id=6 AND q.sort_order=3;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '埋头苦干，不眠不休完成任务', 1.00, 'EXECUTOR' FROM assessment_questions q WHERE q.scale_id=6 AND q.sort_order=3;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'C', '想办法提高效率，找更快的方法', 1.00, 'INNOVATOR' FROM assessment_questions q WHERE q.scale_id=6 AND q.sort_order=3;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'D', '协调各成员进度，确保不脱节', 1.00, 'COORDINATOR' FROM assessment_questions q WHERE q.scale_id=6 AND q.sort_order=3;

INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '归纳总结，推动团队做出决策', 1.00, 'LEADER' FROM assessment_questions q WHERE q.scale_id=6 AND q.sort_order=4;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '评估哪些想法可以落地执行', 1.00, 'EXECUTOR' FROM assessment_questions q WHERE q.scale_id=6 AND q.sort_order=4;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'C', '不断提出新想法，激发讨论', 1.00, 'INNOVATOR' FROM assessment_questions q WHERE q.scale_id=6 AND q.sort_order=4;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'D', '确保每个人的想法都被听到', 1.00, 'COORDINATOR' FROM assessment_questions q WHERE q.scale_id=6 AND q.sort_order=4;

INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '提出明确的目标和路线图', 1.00, 'LEADER' FROM assessment_questions q WHERE q.scale_id=6 AND q.sort_order=5;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '请求明确指令，等待方向后执行', 1.00, 'EXECUTOR' FROM assessment_questions q WHERE q.scale_id=6 AND q.sort_order=5;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'C', '提出各种可能性，拓宽视野', 1.00, 'INNOVATOR' FROM assessment_questions q WHERE q.scale_id=6 AND q.sort_order=5;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'D', '收集团队意见，寻找共同方向', 1.00, 'COORDINATOR' FROM assessment_questions q WHERE q.scale_id=6 AND q.sort_order=5;

INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '带领团队向目标前进', 1.00, 'LEADER' FROM assessment_questions q WHERE q.scale_id=6 AND q.sort_order=6;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '高效、可靠地完成分配的任务', 1.00, 'EXECUTOR' FROM assessment_questions q WHERE q.scale_id=6 AND q.sort_order=6;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'C', '提出突破性的创意和解决方案', 1.00, 'INNOVATOR' FROM assessment_questions q WHERE q.scale_id=6 AND q.sort_order=6;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'D', '维护团队关系，促进沟通合作', 1.00, 'COORDINATOR' FROM assessment_questions q WHERE q.scale_id=6 AND q.sort_order=6;

INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '全面把关，对结果负最终责任', 1.00, 'LEADER' FROM assessment_questions q WHERE q.scale_id=6 AND q.sort_order=7;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '展示详细的执行成果和数据', 1.00, 'EXECUTOR' FROM assessment_questions q WHERE q.scale_id=6 AND q.sort_order=7;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'C', '介绍创新亮点和突破之处', 1.00, 'INNOVATOR' FROM assessment_questions q WHERE q.scale_id=6 AND q.sort_order=7;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'D', '介绍团队协作和分工亮点', 1.00, 'COORDINATOR' FROM assessment_questions q WHERE q.scale_id=6 AND q.sort_order=7;

INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '分配更多资源或调整任务分工', 1.00, 'LEADER' FROM assessment_questions q WHERE q.scale_id=6 AND q.sort_order=8;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '亲自示范或协助对方完成', 1.00, 'EXECUTOR' FROM assessment_questions q WHERE q.scale_id=6 AND q.sort_order=8;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'C', '提供新的方法或工具帮助解决', 1.00, 'INNOVATOR' FROM assessment_questions q WHERE q.scale_id=6 AND q.sort_order=8;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'D', '倾听对方，联系合适的人给予帮助', 1.00, 'COORDINATOR' FROM assessment_questions q WHERE q.scale_id=6 AND q.sort_order=8;

INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '是否达成了既定目标', 1.00, 'LEADER' FROM assessment_questions q WHERE q.scale_id=6 AND q.sort_order=9;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '执行效率和质量是否达标', 1.00, 'EXECUTOR' FROM assessment_questions q WHERE q.scale_id=6 AND q.sort_order=9;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'C', '方案是否有创新和突破性', 1.00, 'INNOVATOR' FROM assessment_questions q WHERE q.scale_id=6 AND q.sort_order=9;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'D', '各方是否充分沟通并达成共识', 1.00, 'COORDINATOR' FROM assessment_questions q WHERE q.scale_id=6 AND q.sort_order=9;

INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'A', '团队在我的带领下完成了目标', 1.00, 'LEADER' FROM assessment_questions q WHERE q.scale_id=6 AND q.sort_order=10;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'B', '我的部分高质量按时完成了', 1.00, 'EXECUTOR' FROM assessment_questions q WHERE q.scale_id=6 AND q.sort_order=10;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'C', '我的创意为项目带来了突破', 1.00, 'INNOVATOR' FROM assessment_questions q WHERE q.scale_id=6 AND q.sort_order=10;
INSERT IGNORE INTO `assessment_options` (question_id, option_label, option_text, score_value, dimension_code)
SELECT q.question_id, 'D', '团队协作顺畅，大家都状态良好', 1.00, 'COORDINATOR' FROM assessment_questions q WHERE q.scale_id=6 AND q.sort_order=10;

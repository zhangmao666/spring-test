const https = require('https');
const fs = require('fs');
const path = require('path');

// 解析命令行参数
const args = process.argv.slice(2);
if (args.length < 2) {
  console.error(JSON.stringify({
    error: '参数不足',
    usage: 'node evaluate-enterprise.js <行业类型> <企业指标JSON>'
  }));
  process.exit(1);
}

const industry = args[0];
let enterpriseMetrics;
try {
  enterpriseMetrics = JSON.parse(args[1]);
} catch (e) {
  console.error(JSON.stringify({ error: '企业指标参数必须是合法JSON格式' }));
  process.exit(1);
}

// 评估指标权重配置（基于企业评价指标.xlsx）
const WEIGHTS = {
  financialHealth: 0.3,    // 财务健康 30%
  marketCustomer: 0.25,    // 市场与客户 25%
  internalOperation: 0.2,  // 内部运营 20%
  innovationGrowth: 0.15,  // 创新与成长 15%
  socialEvaluation: 0.1    // 社会评价 10%
};

/**
 * 调用评估标准接口获取行业基准值
 * @param {string} industry 行业类型
 * @returns {Promise<Object>} 行业基准数据
 */
function getIndustryStandards(industry) {
  return new Promise((resolve, reject) => {
    const options = {
      hostname: 'api.evaluation.example.com', // 实际接口地址请参考references/新增企业评估标准接口.docx
      path: `/v1/standards?industry=${encodeURIComponent(industry)}`,
      method: 'GET',
      timeout: 10000
    };

    const req = https.request(options, (res) => {
      let data = '';
      res.on('data', (chunk) => { data += chunk; });
      res.on('end', () => {
        if (res.statusCode === 200) {
          try {
            resolve(JSON.parse(data));
          } catch (e) {
            reject(new Error('接口返回数据格式错误'));
          }
        } else {
          reject(new Error(`接口请求失败，状态码：${res.statusCode}`));
        }
      });
    });

    req.on('error', (e) => {
      // 接口调用失败时使用默认行业基准（可根据实际情况调整）
      console.warn('接口调用失败，使用默认基准值');
      resolve({
        financial: { revenueGrowthRate: 10, profitMargin: 15, debtRatio: 60 },
        market: { marketShare: 5, customerSatisfaction: 80 },
        operation: { inventoryTurnover: 6, employeeProductivity: 500000 },
        innovation: { r&dRatio: 5, patentCount: 3 },
        social: { taxPaymentRating: 80, socialResponsibilityScore: 70 }
      });
    });

    req.end();
  });
}

/**
 * 计算单项指标得分
 * @param {number} actual 实际值
 * @param {number} benchmark 基准值
 * @param {boolean} higherBetter 是否越高越好
 * @returns {number} 0-100分
 */
function calculateItemScore(actual, benchmark, higherBetter = true) {
  if (higherBetter) {
    return Math.min(100, Math.max(0, (actual / benchmark) * 100));
  } else {
    return Math.min(100, Math.max(0, (benchmark / actual) * 100));
  }
}

/**
 * 执行完整评估流程
 */
async function evaluate() {
  try {
    const standards = await getIndustryStandards(industry);
    
    // 计算各维度得分
    const scores = {
      financialHealth: {
        revenueGrowthRate: calculateItemScore(enterpriseMetrics.financial?.revenueGrowthRate || 0, standards.financial.revenueGrowthRate),
        profitMargin: calculateItemScore(enterpriseMetrics.financial?.profitMargin || 0, standards.financial.profitMargin),
        debtRatio: calculateItemScore(enterpriseMetrics.financial?.debtRatio || 100, standards.financial.debtRatio, false),
        total: 0
      },
      marketCustomer: {
        marketShare: calculateItemScore(enterpriseMetrics.market?.marketShare || 0, standards.market.marketShare),
        customerSatisfaction: calculateItemScore(enterpriseMetrics.market?.customerSatisfaction || 0, standards.market.customerSatisfaction),
        total: 0
      },
      internalOperation: {
        inventoryTurnover: calculateItemScore(enterpriseMetrics.operation?.inventoryTurnover || 0, standards.operation.inventoryTurnover),
        employeeProductivity: calculateItemScore(enterpriseMetrics.operation?.employeeProductivity || 0, standards.operation.employeeProductivity),
        total: 0
      },
      innovationGrowth: {
        rndRatio: calculateItemScore(enterpriseMetrics.innovation?.rndRatio || 0, standards.innovation["r&dRatio"]),
        patentCount: calculateItemScore(enterpriseMetrics.innovation?.patentCount || 0, standards.innovation.patentCount),
        total: 0
      },
      socialEvaluation: {
        taxPaymentRating: calculateItemScore(enterpriseMetrics.social?.taxPaymentRating || 0, standards.social.taxPaymentRating),
        socialResponsibilityScore: calculateItemScore(enterpriseMetrics.social?.socialResponsibilityScore || 0, standards.social.socialResponsibilityScore),
        total: 0
      }
    };

    // 计算各维度总分
    scores.financialHealth.total = (
      scores.financialHealth.revenueGrowthRate * 0.4 +
      scores.financialHealth.profitMargin * 0.3 +
      scores.financialHealth.debtRatio * 0.3
    );

    scores.marketCustomer.total = (
      scores.marketCustomer.marketShare * 0.5 +
      scores.marketCustomer.customerSatisfaction * 0.5
    );

    scores.internalOperation.total = (
      scores.internalOperation.inventoryTurnover * 0.4 +
      scores.internalOperation.employeeProductivity * 0.6
    );

    scores.innovationGrowth.total = (
      scores.innovationGrowth.rndRatio * 0.6 +
      scores.innovationGrowth.patentCount * 0.4
    );

    scores.socialEvaluation.total = (
      scores.socialEvaluation.taxPaymentRating * 0.5 +
      scores.socialEvaluation.socialResponsibilityScore * 0.5
    );

    // 计算综合总得分
    const totalScore = (
      scores.financialHealth.total * WEIGHTS.financialHealth +
      scores.marketCustomer.total * WEIGHTS.marketCustomer +
      scores.internalOperation.total * WEIGHTS.internalOperation +
      scores.innovationGrowth.total * WEIGHTS.innovationGrowth +
      scores.socialEvaluation.total * WEIGHTS.socialEvaluation
    ).toFixed(2);

    // 确定评估等级
    let level;
    if (totalScore >= 90) level = '优秀';
    else if (totalScore >= 80) level = '良好';
    else if (totalScore >= 70) level = '中等';
    else if (totalScore >= 60) level = '合格';
    else level = '不合格';

    // 输出评估结果
    console.log(JSON.stringify({
      success: true,
      industry,
      totalScore: parseFloat(totalScore),
      level,
      dimensionScores: scores,
      weights: WEIGHTS,
      standards: standards,
      evaluationTime: new Date().toISOString()
    }, null, 2));

  } catch (error) {
    console.error(JSON.stringify({ error: error.message }));
    process.exit(1);
  }
}

evaluate();
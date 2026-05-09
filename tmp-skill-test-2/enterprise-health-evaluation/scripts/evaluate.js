const axios = require('axios');
const xlsx = require('xlsx');
const path = require('path');

// 读取本地指标权重文件
function getIndicatorWeights() {
    const workbook = xlsx.readFile(path.join(__dirname, '../references/企业评价指标.xlsx'));
    const sheet = workbook.Sheets[workbook.SheetNames[0]];
    const data = xlsx.utils.sheet_to_json(sheet);
    
    const weights = {};
    data.forEach(row => {
        weights[row['四级指标名称']] = parseFloat(row['权重']);
    });
    return weights;
}

// 获取行业评估标准
async function getIndustryStandard(industry) {
    try {
        const response = await axios.get('https://api.example.com/enterprise/evaluation/standard', {
            params: { industry }
        });
        return response.data;
    } catch (error) {
        throw new Error(`获取行业标准失败: ${error.message}`);
    }
}

// 计算得分
function calculateScore(enterpriseData, industryStandard, weights) {
    const dimensionScores = {};
    let totalScore = 0;

    Object.keys(enterpriseData).forEach(indicator => {
        if (weights[indicator] && industryStandard[indicator]) {
            const value = enterpriseData[indicator];
            const standard = industryStandard[indicator];
            let score = 0;

            // 根据标准计算得分（可根据实际评分逻辑调整）
            if (value >= standard.excellent) {
                score = 100;
            } else if (value >= standard.good) {
                score = 80;
            } else if (value >= standard.pass) {
                score = 60;
            } else {
                score = 40;
            }

            const weightedScore = score * weights[indicator];
            dimensionScores[indicator] = {
                rawValue: value,
                standard: standard,
                score: score,
                weightedScore: weightedScore
            };
            totalScore += weightedScore;
        }
    });

    return {
        dimensionScores,
        totalScore: Math.round(totalScore * 100) / 100,
        level: totalScore >= 90 ? '优秀' : totalScore >= 70 ? '良好' : totalScore >= 60 ? '合格' : '不合格'
    };
}

// 主函数
async function main() {
    const args = process.argv.slice(2);
    if (args.length < 2) {
        console.error(JSON.stringify({ error: '参数不足，用法: node evaluate.js <行业> <企业数据JSON>' }));
        process.exit(1);
    }

    const industry = args[0];
    const enterpriseData = JSON.parse(args[1]);

    try {
        const weights = getIndicatorWeights();
        const industryStandard = await getIndustryStandard(industry);
        const result = calculateScore(enterpriseData, industryStandard, weights);
        
        console.log(JSON.stringify(result, null, 2));
    } catch (error) {
        console.error(JSON.stringify({ error: error.message }));
        process.exit(1);
    }
}

main();

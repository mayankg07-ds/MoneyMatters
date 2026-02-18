// Quick API integration test
import axios from 'axios';

const API_BASE = 'http://localhost:8082/api/v1';

async function testAPIs() {
    console.log('🧪 Testing MoneyMatters API Integration...\n');

    // Test 1: Get Holdings for User 1
    try {
        console.log('📊 Test 1: GET /portfolio/holdings/user/1');
        const holdings = await axios.get(`${API_BASE}/portfolio/holdings/user/1`);
        console.log('✅ Success:', holdings.data);
    } catch (error) {
        console.log('❌ Error:', error.response?.status, error.response?.data || error.message);
    }

    // Test 2: Get Portfolio Summary
    try {
        console.log('\n📈 Test 2: GET /portfolio/holdings/user/1/summary');
        const summary = await axios.get(`${API_BASE}/portfolio/holdings/user/1/summary`);
        console.log('✅ Success:', summary.data);
    } catch (error) {
        console.log('❌ Error:', error.response?.status, error.response?.data || error.message);
    }

    // Test 3: Get Transactions
    try {
        console.log('\n💸 Test 3: GET /portfolio/transactions/user/1');
        const transactions = await axios.get(`${API_BASE}/portfolio/transactions/user/1`);
        console.log('✅ Success:', transactions.data);
    } catch (error) {
        console.log('❌ Error:', error.response?.status, error.response?.data || error.message);
    }

    // Test 4: Get Analytics
    try {
        console.log('\n📉 Test 4: GET /portfolio/analytics/user/1');
        const analytics = await axios.get(`${API_BASE}/portfolio/analytics/user/1`);
        console.log('✅ Success:', analytics.data);
    } catch (error) {
        console.log('❌ Error:', error.response?.status, error.response?.data || error.message);
    }

    // Test 5: SIP Calculator
    try {
        console.log('\n🧮 Test 5: POST /calculators/sip-stepup/calculate');
        const sipResult = await axios.post(`${API_BASE}/calculators/sip-stepup/calculate`, {
            monthlyInvestment: 10000,
            annualStepUp: 10,
            expectedReturn: 12,
            timePeriod: 10
        });
        console.log('✅ Success:', sipResult.data);
    } catch (error) {
        console.log('❌ Error:', error.response?.status, error.response?.data || error.message);
    }

    console.log('\n✨ API Integration Test Complete!');
}

testAPIs();

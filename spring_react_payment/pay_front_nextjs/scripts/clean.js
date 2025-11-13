const fs = require('fs');
const path = require('path');

const dirsToClean = [
  path.join(__dirname, '..', '.next'),
  path.join(__dirname, '..', 'node_modules', '.cache'),
  path.join(__dirname, '..', '.swc'),
];

function removeDir(dir, retries = 3) {
  if (!fs.existsSync(dir)) {
    return;
  }
  
  for (let i = 0; i < retries; i++) {
    try {
      fs.rmSync(dir, { recursive: true, force: true, maxRetries: 3, retryDelay: 100 });
      console.log(`✓ 삭제됨: ${dir}`);
      return;
    } catch (error) {
      if (i === retries - 1) {
        console.error(`✗ 삭제 실패: ${dir}`, error.message);
      } else {
        const start = Date.now();
        while (Date.now() - start < 500) {
        }
      }
    }
  }
}

console.log('캐시 정리 시작...');
dirsToClean.forEach((dir) => {
  removeDir(dir);
});
console.log('캐시 정리 완료');


package BookingService.BookingService.service;

import BookingService.BookingService.dto.request.QuizAnswerRequest;
import BookingService.BookingService.dto.request.QuizQuestionRequest;
import BookingService.BookingService.dto.response.QuizAnswerResponse;
import BookingService.BookingService.dto.response.QuizQuestionResponse;
import BookingService.BookingService.entity.*;
import BookingService.BookingService.enums.SkinType;
import BookingService.BookingService.exception.AppException;
import BookingService.BookingService.exception.ErrorCode;
import BookingService.BookingService.repository.QuizAnswerRepository;
import BookingService.BookingService.repository.QuizQuestionRepository;
import BookingService.BookingService.repository.QuizResultRepository;
import BookingService.BookingService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizAnswerRepository quizAnswerRepository;
    private final QuizResultRepository quizResultRepository;
    private final UserRepository userRepository;
    private final ServiceEntityService serviceEntityService;

    public Map<String, Object> processQuiz(Map<Long, Long> selectedAnswers) {
        Map<SkinType, Integer> skinTypeScores = new HashMap<>();
        for (SkinType type : SkinType.values()) {
            skinTypeScores.put(type, 0);
        }

        List<Map<String, Object>> userResponses = new ArrayList<>();

        for (Map.Entry<Long, Long> entry : selectedAnswers.entrySet()) {
            Long questionId = entry.getKey();
            Long answerId = entry.getValue();

            QuizAnswer answer = quizAnswerRepository.findById(answerId)
                    .orElseThrow(() -> new AppException(ErrorCode.QUIZ_ANSWER_NOT_FOUND));
            QuizQuestion question = quizQuestionRepository.findById(questionId)
                    .orElseThrow(() -> new AppException(ErrorCode.QUIZ_QUESTION_NOT_FOUND));

            SkinType skinType = determineSkinTypeFromAnswer(answer);
            int score = answer.getScore();

            skinTypeScores.put(skinType, skinTypeScores.get(skinType) + score);

            Map<String, Object> response = new HashMap<>();
            response.put("questionId", question.getId());
            response.put("questionText", question.getQuestion());
            response.put("answerId", answer.getId());
            response.put("answerText", answer.getAnswer());
            response.put("score", score);
            response.put("skinType", skinType.toString());
            userResponses.add(response);
        }

        SkinType detectedSkinType = Collections.max(skinTypeScores.entrySet(), Map.Entry.comparingByValue()).getKey();

        List<ServiceEntity> recommendedServices = serviceEntityService.getServicesBySkinType(detectedSkinType);
        String recommendedServiceNames = recommendedServices.stream()
                .map(ServiceEntity::getName)
                .collect(Collectors.joining(", "));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        QuizResult quizResult = QuizResult.builder()
                .user(user)
                .detectedSkinType(detectedSkinType)
                .recommendedService(recommendedServiceNames)
                .createdAt(LocalDateTime.now())
                .build();
        quizResultRepository.save(quizResult);

        Map<String, Object> result = new HashMap<>();
        result.put("userResponses", userResponses);
        result.put("skinTypeScores", skinTypeScores);
        result.put("detectedSkinType", detectedSkinType);
        result.put("recommendedServices", recommendedServices);
        return result;
    }

    private SkinType determineSkinTypeFromAnswer(QuizAnswer answer) {
        if (answer.getSkinType() != null) {
            return answer.getSkinType();
        }

        String answerText = answer.getAnswer();
        String questionText = answer.getQuestion().getQuestion();

        return switch (questionText) {
            case "Da bạn có thường xuyên bị bóng dầu không?" -> switch (answerText) {
                case "Luôn luôn" -> SkinType.OILY;
                case "Thỉnh thoảng" -> SkinType.COMBINATION;
                case "Hiếm khi" -> SkinType.NORMAL;
                case "Không bao giờ" -> SkinType.DRY;
                default -> throw new IllegalArgumentException("Unknown answer: " + answerText);
            };
            case "Bạn cảm thấy da mình thế nào sau khi rửa mặt?" -> switch (answerText) {
                case "Căng rát" -> SkinType.DRY;
                case "Mềm mại, không khó chịu" -> SkinType.NORMAL;
                case "Dầu xuất hiện sau vài giờ" -> SkinType.OILY;
                case "Khô ở một số vùng, dầu ở vùng khác" -> SkinType.COMBINATION;
                default -> throw new IllegalArgumentException("Unknown answer: " + answerText);
            };
            case "Bạn có dễ bị mụn không?" -> switch (answerText) {
                case "Thường xuyên" -> SkinType.OILY;
                case "Đôi khi, vào những thời điểm nhất định" -> SkinType.COMBINATION;
                case "Rất hiếm" -> SkinType.NORMAL;
                case "Hầu như không bao giờ" -> SkinType.DRY;
                default -> throw new IllegalArgumentException("Unknown answer: " + answerText);
            };
            case "Lỗ chân lông của bạn có kích thước thế nào?" -> switch (answerText) {
                case "To và dễ thấy" -> SkinType.OILY;
                case "Nhỏ nhưng rõ ràng ở vùng chữ T" -> SkinType.COMBINATION;
                case "Nhỏ, khó thấy" -> SkinType.NORMAL;
                case "Rất nhỏ hoặc không thấy" -> SkinType.DRY;
                default -> throw new IllegalArgumentException("Unknown answer: " + answerText);
            };
            case "Da bạn có bị bong tróc không?" -> switch (answerText) {
                case "Thường xuyên" -> SkinType.DRY;
                case "Đôi khi vào mùa đông" -> SkinType.NORMAL;
                case "Không bao giờ" -> SkinType.OILY;
                case "Chỉ ở một số vùng" -> SkinType.COMBINATION;
                default -> throw new IllegalArgumentException("Unknown answer: " + answerText);
            };
            case "Bạn có dễ bị kích ứng, đỏ da không?" -> switch (answerText) {
                case "Rất dễ" -> SkinType.SENSITIVE;
                case "Thỉnh thoảng" -> SkinType.NORMAL;
                case "Hiếm khi" -> SkinType.OILY;
                case "Gần như không bao giờ" -> SkinType.COMBINATION;
                default -> throw new IllegalArgumentException("Unknown answer: " + answerText);
            };
            case "Khi bạn thử sản phẩm dưỡng da mới, da bạn phản ứng thế nào?" -> switch (answerText) {
                case "Dễ bị kích ứng, đỏ" -> SkinType.SENSITIVE;
                case "Cần thời gian thích nghi" -> SkinType.NORMAL;
                case "Không có phản ứng" -> SkinType.OILY;
                case "Chỉ phản ứng với một số thành phần" -> SkinType.COMBINATION;
                default -> throw new IllegalArgumentException("Unknown answer: " + answerText);
            };
            case "Bạn có thấy da mình thay đổi theo thời tiết không?" -> switch (answerText) {
                case "Rất nhạy cảm với thời tiết" -> SkinType.SENSITIVE;
                case "Chỉ thay đổi nhẹ" -> SkinType.NORMAL;
                case "Mùa hè nhiều dầu, mùa đông khô" -> SkinType.COMBINATION;
                case "Không ảnh hưởng nhiều" -> SkinType.OILY;
                default -> throw new IllegalArgumentException("Unknown answer: " + answerText);
            };
            case "Bạn có cần dưỡng ẩm hàng ngày không?" -> switch (answerText) {
                case "Không thể thiếu" -> SkinType.DRY;
                case "Cần nhưng không nhiều" -> SkinType.NORMAL;
                case "Chỉ vùng khô" -> SkinType.COMBINATION;
                case "Không cần hoặc rất ít" -> SkinType.OILY;
                default -> throw new IllegalArgumentException("Unknown answer: " + answerText);
            };
            case "Sau 6 tiếng không rửa mặt, da bạn trông thế nào?" -> switch (answerText) {
                case "Rất dầu, bóng nhờn" -> SkinType.OILY;
                case "Dầu ở vùng chữ T" -> SkinType.COMBINATION;
                case "Bình thường" -> SkinType.NORMAL;
                case "Căng khô" -> SkinType.DRY;
                default -> throw new IllegalArgumentException("Unknown answer: " + answerText);
            };
            default -> throw new IllegalArgumentException("Unknown question: " + questionText);
        };
    }

    public List<QuizResult> getUserQuizHistory(User user) {
        return quizResultRepository.findByUser(user);
    }

    public void populateQuizData() {
        if (quizQuestionRepository.count() > 0) {
            return;
        }

        // Tạo danh sách câu hỏi
        List<QuizQuestion> questions = new ArrayList<>();
        questions.add(new QuizQuestion(null, "Da bạn có thường xuyên bị bóng dầu không?"));
        questions.add(new QuizQuestion(null, "Bạn cảm thấy da mình thế nào sau khi rửa mặt?"));
        questions.add(new QuizQuestion(null, "Bạn có dễ bị mụn không?"));
        questions.add(new QuizQuestion(null, "Lỗ chân lông của bạn có kích thước thế nào?"));
        questions.add(new QuizQuestion(null, "Da bạn có bị bong tróc không?"));
        questions.add(new QuizQuestion(null, "Bạn có dễ bị kích ứng, đỏ da không?"));
        questions.add(new QuizQuestion(null, "Khi bạn thử sản phẩm dưỡng da mới, da bạn phản ứng thế nào?"));
        questions.add(new QuizQuestion(null, "Bạn có thấy da mình thay đổi theo thời tiết không?"));
        questions.add(new QuizQuestion(null, "Bạn có cần dưỡng ẩm hàng ngày không?"));
        questions.add(new QuizQuestion(null, "Sau 6 tiếng không rửa mặt, da bạn trông thế nào?"));

        // Lưu danh sách câu hỏi vào cơ sở dữ liệu
        List<QuizQuestion> savedQuestions = quizQuestionRepository.saveAll(questions);



        // Tạo danh sách đáp án
        List<QuizAnswer> answers = new ArrayList<>();

        // Câu 1: "Da bạn có thường xuyên bị bóng dầu không?"
        answers.add(QuizAnswer.builder()
                .id(null)
                .answer("Luôn luôn")
                .score(3)
                .question(savedQuestions.get(0))
                .skinType(SkinType.OILY)
                .build());
        answers.add(QuizAnswer.builder()
                .id(null)
                .answer("Thỉnh thoảng")
                .score(2)
                .question(savedQuestions.get(0))
                .skinType(SkinType.COMBINATION)
                .build());
        answers.add(QuizAnswer.builder()
                .id(null)
                .answer("Hiếm khi")
                .score(1)
                .question(savedQuestions.get(0))
                .skinType(SkinType.NORMAL)
                .build());
        answers.add(QuizAnswer.builder()
                .id(null)
                .answer("Không bao giờ")
                .score(0)
                .question(savedQuestions.get(0))
                .skinType(SkinType.DRY)
                .build());

        // Câu 2: "Bạn cảm thấy da mình thế nào sau khi rửa mặt?"
        answers.add(QuizAnswer.builder()
                .id(null)
                .answer("Căng rát")
                .score(3)
                .question(savedQuestions.get(1))
                .skinType(SkinType.DRY)
                .build());
        answers.add(QuizAnswer.builder()
                .id(null)
                .answer("Mềm mại, không khó chịu")
                .score(2)
                .question(savedQuestions.get(1))
                .skinType(SkinType.NORMAL)
                .build());
        answers.add(QuizAnswer.builder()
                .id(null)
                .answer("Dầu xuất hiện sau vài giờ")
                .score(3)
                .question(savedQuestions.get(1))
                .skinType(SkinType.OILY)
                .build());
        answers.add(QuizAnswer.builder()
                .id(null)
                .answer("Khô ở một số vùng, dầu ở vùng khác")
                .score(2)
                .question(savedQuestions.get(1))
                .skinType(SkinType.COMBINATION)
                .build());

        // Câu 3: "Bạn có dễ bị mụn không?"
        answers.add(QuizAnswer.builder()
                .id(null)
                .answer("Thường xuyên")
                .score(3)
                .question(savedQuestions.get(2))
                .skinType(SkinType.OILY)
                .build());
        answers.add(QuizAnswer.builder()
                .id(null)
                .answer("Đôi khi, vào những thời điểm nhất định")
                .score(2)
                .question(savedQuestions.get(2))
                .skinType(SkinType.COMBINATION)
                .build());
        answers.add(QuizAnswer.builder()
                .id(null)
                .answer("Rất hiếm")
                .score(1)
                .question(savedQuestions.get(2))
                .skinType(SkinType.NORMAL)
                .build());
        answers.add(QuizAnswer.builder()
                .id(null)
                .answer("Hầu như không bao giờ")
                .score(0)
                .question(savedQuestions.get(2))
                .skinType(SkinType.DRY)
                .build());

        // Câu 4: "Lỗ chân lông của bạn có kích thước thế nào?"
        answers.add(QuizAnswer.builder()
                .id(null)
                .answer("To và dễ thấy")
                .score(3)
                .question(savedQuestions.get(3))
                .skinType(SkinType.OILY)
                .build());
        answers.add(QuizAnswer.builder()
                .id(null)
                .answer("Nhỏ nhưng rõ ràng ở vùng chữ T")
                .score(2)
                .question(savedQuestions.get(3))
                .skinType(SkinType.COMBINATION)
                .build());
        answers.add(QuizAnswer.builder()
                .id(null)
                .answer("Nhỏ, khó thấy")
                .score(1)
                .question(savedQuestions.get(3))
                .skinType(SkinType.NORMAL)
                .build());
        answers.add(QuizAnswer.builder()
                .id(null)
                .answer("Rất nhỏ hoặc không thấy")
                .score(0)
                .question(savedQuestions.get(3))
                .skinType(SkinType.DRY)
                .build());

        // Câu 5: "Da bạn có bị bong tróc không?"
        answers.add(QuizAnswer.builder()
                .id(null)
                .answer("Thường xuyên")
                .score(3)
                .question(savedQuestions.get(4))
                .skinType(SkinType.DRY)
                .build());
        answers.add(QuizAnswer.builder()
                .id(null)
                .answer("Đôi khi vào mùa đông")
                .score(2)
                .question(savedQuestions.get(4))
                .skinType(SkinType.NORMAL)
                .build());
        answers.add(QuizAnswer.builder()
                .id(null)
                .answer("Không bao giờ")
                .score(0)
                .question(savedQuestions.get(4))
                .skinType(SkinType.OILY)
                .build());
        answers.add(QuizAnswer.builder()
                .id(null)
                .answer("Chỉ ở một số vùng")
                .score(2)
                .question(savedQuestions.get(4))
                .skinType(SkinType.COMBINATION)
                .build());

        // Câu 6: "Bạn có dễ bị kích ứng, đỏ da không?"
        answers.add(QuizAnswer.builder()
                .id(null)
                .answer("Rất dễ")
                .score(3)
                .question(savedQuestions.get(5))
                .skinType(SkinType.SENSITIVE)
                .build());
        answers.add(QuizAnswer.builder()
                .id(null)
                .answer("Thỉnh thoảng")
                .score(2)
                .question(savedQuestions.get(5))
                .skinType(SkinType.NORMAL)
                .build());
        answers.add(QuizAnswer.builder()
                .id(null)
                .answer("Hiếm khi")
                .score(1)
                .question(savedQuestions.get(5))
                .skinType(SkinType.OILY)
                .build());
        answers.add(QuizAnswer.builder()
                .id(null)
                .answer("Gần như không bao giờ")
                .score(0)
                .question(savedQuestions.get(5))
                .skinType(SkinType.COMBINATION)
                .build());

        // Câu 7: "Khi bạn thử sản phẩm dưỡng da mới, da bạn phản ứng thế nào?"
        answers.add(QuizAnswer.builder()
                .id(null)
                .answer("Dễ bị kích ứng, đỏ")
                .score(3)
                .question(savedQuestions.get(6))
                .skinType(SkinType.SENSITIVE)
                .build());
        answers.add(QuizAnswer.builder()
                .id(null)
                .answer("Cần thời gian thích nghi")
                .score(2)
                .question(savedQuestions.get(6))
                .skinType(SkinType.NORMAL)
                .build());
        answers.add(QuizAnswer.builder()
                .id(null)
                .answer("Không có phản ứng")
                .score(1)
                .question(savedQuestions.get(6))
                .skinType(SkinType.OILY)
                .build());
        answers.add(QuizAnswer.builder()
                .id(null)
                .answer("Chỉ phản ứng với một số thành phần")
                .score(2)
                .question(savedQuestions.get(6))
                .skinType(SkinType.COMBINATION)
                .build());

        // Câu 8: "Bạn có thấy da mình thay đổi theo thời tiết không?"
        answers.add(QuizAnswer.builder()
                .id(null)
                .answer("Rất nhạy cảm với thời tiết")
                .score(3)
                .question(savedQuestions.get(7))
                .skinType(SkinType.SENSITIVE)
                .build());
        answers.add(QuizAnswer.builder()
                .id(null)
                .answer("Chỉ thay đổi nhẹ")
                .score(2)
                .question(savedQuestions.get(7))
                .skinType(SkinType.NORMAL)
                .build());
        answers.add(QuizAnswer.builder()
                .id(null)
                .answer("Mùa hè nhiều dầu, mùa đông khô")
                .score(3)
                .question(savedQuestions.get(7))
                .skinType(SkinType.COMBINATION)
                .build());
        answers.add(QuizAnswer.builder()
                .id(null)
                .answer("Không ảnh hưởng nhiều")
                .score(1)
                .question(savedQuestions.get(7))
                .skinType(SkinType.OILY)
                .build());

        // Câu 9: "Bạn có cần dưỡng ẩm hàng ngày không?"
        answers.add(QuizAnswer.builder()
                .id(null)
                .answer("Không thể thiếu")
                .score(3)
                .question(savedQuestions.get(8))
                .skinType(SkinType.DRY)
                .build());
        answers.add(QuizAnswer.builder()
                .id(null)
                .answer("Cần nhưng không nhiều")
                .score(2)
                .question(savedQuestions.get(8))
                .skinType(SkinType.NORMAL)
                .build());
        answers.add(QuizAnswer.builder()
                .id(null)
                .answer("Chỉ vùng khô")
                .score(2)
                .question(savedQuestions.get(8))
                .skinType(SkinType.COMBINATION)
                .build());
        answers.add(QuizAnswer.builder()
                .id(null)
                .answer("Không cần hoặc rất ít")
                .score(1)
                .question(savedQuestions.get(8))
                .skinType(SkinType.OILY)
                .build());

        // Câu 10: "Sau 6 tiếng không rửa mặt, da bạn trông thế nào?"
        answers.add(QuizAnswer.builder()
                .id(null)
                .answer("Rất dầu, bóng nhờn")
                .score(3)
                .question(savedQuestions.get(9))
                .skinType(SkinType.OILY)
                .build());
        answers.add(QuizAnswer.builder()
                .id(null)
                .answer("Dầu ở vùng chữ T")
                .score(2)
                .question(savedQuestions.get(9))
                .skinType(SkinType.COMBINATION)
                .build());
        answers.add(QuizAnswer.builder()
                .id(null)
                .answer("Bình thường")
                .score(2)
                .question(savedQuestions.get(9))
                .skinType(SkinType.NORMAL)
                .build());
        answers.add(QuizAnswer.builder()
                .id(null)
                .answer("Căng khô")
                .score(3)
                .question(savedQuestions.get(9))
                .skinType(SkinType.DRY)
                .build());

        quizAnswerRepository.saveAll(answers);
    }

    public List<Map<String, Object>> getQuizQuestionsWithAnswers() {
        List<QuizQuestion> questions = quizQuestionRepository.findAll();
        if (questions.isEmpty()) {
            throw new AppException(ErrorCode.QUIZ_QUESTION_NOT_FOUND);
        }

        return questions.stream()
                .map(question -> {
                    List<QuizAnswer> answers = quizAnswerRepository.findByQuestion(question);
                    if (answers.isEmpty()) {
                        throw new AppException(ErrorCode.QUIZ_ANSWER_NOT_FOUND);
                    }

                    Map<String, Object> questionData = new HashMap<>();
                    questionData.put("questionId", question.getId());
                    questionData.put("questionText", question.getQuestion());

                    List<Map<String, Object>> answerList = answers.stream()
                            .map(answer -> {
                                Map<String, Object> answerData = new HashMap<>();
                                answerData.put("answerId", answer.getId());
                                answerData.put("answerText", answer.getAnswer());
                                answerData.put("score", answer.getScore());
                                answerData.put("skinType", answer.getSkinType().toString());
                                return answerData;
                            })
                            .collect(Collectors.toList());

                    questionData.put("answers", answerList);
                    return questionData;
                })
                .collect(Collectors.toList());
    }

    public List<ServiceEntity> getRecommendedServicesFromLatestQuiz() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        User user = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        List<QuizResult> quizHistory = quizResultRepository.findByUser(user);
        if (quizHistory.isEmpty()) {
            throw new AppException(ErrorCode.QUIZ_RESULT_NOT_FOUND);
        }

        QuizResult latestQuiz = quizHistory.stream()
                .sorted(Comparator.comparing(QuizResult::getCreatedAt).reversed())
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.QUIZ_RESULT_NOT_FOUND));

        SkinType detectedSkinType = latestQuiz.getDetectedSkinType();
        return serviceEntityService.getServicesBySkinType(detectedSkinType);
    }

    public QuizQuestionResponse createQuestion(QuizQuestionRequest request) {
        if (request.getAnswers() == null || request.getAnswers().size() != 4) {
            throw new AppException(ErrorCode.INVALID_INPUT);
        }

        QuizQuestion quizQuestion = QuizQuestion.builder()
                .question(request.getQuestionText())
                .build();
        QuizQuestion savedQuestion = quizQuestionRepository.save(quizQuestion);

        List<QuizAnswer> answers = request.getAnswers().stream().map(answerRequest -> {
            if (answerRequest.getSkinType() == null) {
                throw new AppException(ErrorCode.INVALID_INPUT);
            }
            return QuizAnswer.builder()
                    .answer(answerRequest.getAnswerText())
                    .score(answerRequest.getScore())
                    .skinType(answerRequest.getSkinType())
                    .question(savedQuestion)
                    .build();
        }).collect(Collectors.toList());
        quizAnswerRepository.saveAll(answers);

        QuizQuestionResponse response = new QuizQuestionResponse();
        response.setId(savedQuestion.getId());
        response.setQuestionText(savedQuestion.getQuestion());

        List<QuizAnswerResponse> answerResponses = answers.stream().map(answer -> {
            QuizAnswerResponse answerResponse = new QuizAnswerResponse();
            answerResponse.setId(answer.getId());
            answerResponse.setAnswerText(answer.getAnswer());
            answerResponse.setScore(answer.getScore());
            return answerResponse;
        }).collect(Collectors.toList());
        response.setAnswers(answerResponses);

        return response;
    }

    public List<QuizQuestionResponse> getAllQuestions() {
        return quizQuestionRepository.findAll().stream()
                .map(question -> {
                    QuizQuestionResponse response = new QuizQuestionResponse();
                    response.setId(question.getId());
                    response.setQuestionText(question.getQuestion());

                    List<QuizAnswer> answers = quizAnswerRepository.findByQuestion(question);
                    List<QuizAnswerResponse> answerResponses = answers.stream().map(answer -> {
                        QuizAnswerResponse answerResponse = new QuizAnswerResponse();
                        answerResponse.setId(answer.getId());
                        answerResponse.setAnswerText(answer.getAnswer());
                        answerResponse.setScore(answer.getScore());
                        return answerResponse;
                    }).collect(Collectors.toList());
                    response.setAnswers(answerResponses);

                    return response;
                })
                .collect(Collectors.toList());
    }

    public QuizQuestionResponse getQuestionById(Long id) {
        QuizQuestion question = quizQuestionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.QUIZ_QUESTION_NOT_FOUND));

        QuizQuestionResponse response = new QuizQuestionResponse();
        response.setId(question.getId());
        response.setQuestionText(question.getQuestion());

        List<QuizAnswer> answers = quizAnswerRepository.findByQuestion(question);
        List<QuizAnswerResponse> answerResponses = answers.stream().map(answer -> {
            QuizAnswerResponse answerResponse = new QuizAnswerResponse();
            answerResponse.setId(answer.getId());
            answerResponse.setAnswerText(answer.getAnswer());
            answerResponse.setScore(answer.getScore());
            return answerResponse;
        }).collect(Collectors.toList());
        response.setAnswers(answerResponses);

        return response;
    }

    public QuizQuestionResponse updateQuestion(Long id, QuizQuestionRequest request) {
        QuizQuestion quizQuestion = quizQuestionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.QUIZ_QUESTION_NOT_FOUND));

        quizQuestion.setQuestion(request.getQuestionText());
        QuizQuestion updatedQuestion = quizQuestionRepository.save(quizQuestion);

        List<QuizAnswer> existingAnswers = quizAnswerRepository.findByQuestion(quizQuestion);
        quizAnswerRepository.deleteAll(existingAnswers);

        List<QuizAnswer> newAnswers = request.getAnswers().stream().map(answerRequest -> {
            if (answerRequest.getSkinType() == null) {
                throw new AppException(ErrorCode.INVALID_INPUT);
            }
            return QuizAnswer.builder()
                    .answer(answerRequest.getAnswerText())
                    .score(answerRequest.getScore())
                    .skinType(answerRequest.getSkinType())
                    .question(updatedQuestion)
                    .build();
        }).collect(Collectors.toList());
        quizAnswerRepository.saveAll(newAnswers);

        QuizQuestionResponse response = new QuizQuestionResponse();
        response.setId(updatedQuestion.getId());
        response.setQuestionText(updatedQuestion.getQuestion());

        List<QuizAnswerResponse> answerResponses = newAnswers.stream().map(answer -> {
            QuizAnswerResponse answerResponse = new QuizAnswerResponse();
            answerResponse.setId(answer.getId());
            answerResponse.setAnswerText(answer.getAnswer());
            answerResponse.setScore(answer.getScore());
            return answerResponse;
        }).collect(Collectors.toList());
        response.setAnswers(answerResponses);

        return response;
    }

    public void deleteQuestion(Long id) {
        QuizQuestion quizQuestion = quizQuestionRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.QUIZ_QUESTION_NOT_FOUND));

        List<QuizAnswer> relatedAnswers = quizAnswerRepository.findByQuestion(quizQuestion);
        if (!relatedAnswers.isEmpty()) {
            quizAnswerRepository.deleteAll(relatedAnswers);
        }
        quizQuestionRepository.deleteById(id);
    }
}